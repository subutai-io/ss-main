package io.subutai.core.security.impl.crypto;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.security.api.dao.SecretKeyStoreDAO;
import io.subutai.core.security.api.dao.SecurityManagerDAO;
import io.subutai.core.security.impl.model.SecurityKeyData;



/**
 * Implementation of KeyManager API
 */
public class KeyManagerImpl implements KeyManager
{
    private static final Logger LOG = LoggerFactory.getLogger( KeyManagerImpl.class );

    private SecurityManagerDAO securityManagerDAO = null;
    private SecretKeyStoreDAO secretKeyStoreDAO = null;
    private KeyServer keyServer = null;
    private SecurityKeyData keyData = null;


    /* *****************************
     *
     */
    public KeyManagerImpl( SecurityManagerDAO securityManagerDAO, SecretKeyStoreDAO secretKeyStoreDAO,
                           KeyServer keyServer, SecurityKeyData securityKeyData )
    {
        this.keyData = securityKeyData;
        this.securityManagerDAO = securityManagerDAO;
        this.keyServer = keyServer;
        this.secretKeyStoreDAO = secretKeyStoreDAO;

        // Create Key Identity Record , save Public key in the KeyStore.
        init();
    }


    /* *****************************
     *
     */
    public KeyManagerImpl( SecurityManagerDAO securityManagerDAO, KeyServer keyServer, String publicKeyringFile,
                           String secretKeyringFile, String secretKeyringPwd, String manHostId,
                           String manHostKeyFingerprint )
    {
        this.securityManagerDAO = securityManagerDAO;
        this.keyServer = keyServer;

        keyData = new SecurityKeyData();
        keyData.setManHostId( manHostId );
        keyData.setPublicKeyringFile( publicKeyringFile );
        keyData.setSecretKeyringFile( secretKeyringFile );
        keyData.setSecretKeyringPwd( secretKeyringPwd );
        keyData.setManHostKeyFingerprint( manHostKeyFingerprint );

        // Create Key Identity Record , save Public key in the KeyStore.
        init();
    }


    /* *****************************
     *
     */
    private void init()
    {
        try
        {
            // Create New KeyRings;
            LOG.info( "******** Creating Key new keyring *******" );

            saveKeyPair( keyData.getManHostId(),(short)1, generateKeyPair( keyData.getManHostId(), false ) );
        }
        catch ( Exception ex )
        {
            LOG.error( " **** Error, Creating PGPPublicKeyRing/PGPSecretKeyRing **** :" + ex.toString() );
        }
    }


    /* ***************************************************************
     *
     */
    @Override
    public void saveSecretKeyRing( String hostId, short type, PGPSecretKeyRing secretKeyRing)
    {
        try
        {
            PGPPublicKey publicKey = secretKeyRing.getPublicKey();

            if ( publicKey != null )
            {
                // Store secretKey
                String fingerprint = PGPKeyUtil.getFingerprint( publicKey.getFingerprint() );
                String pwd = keyData.getSecretKeyringPwd();

                secretKeyStoreDAO.saveSecretKeyRing( fingerprint, secretKeyRing.getEncoded(), pwd, type);
                securityManagerDAO.saveKeyIdentityData( hostId, fingerprint, "", type);
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error storing Public key:" + ex.toString() );
        }
    }


    /* ***************************************************************
     *
     */
    @Override
    public void savePublicKeyRing( String hostId,short type, String keyringAsASCII )
    {
        try
        {
            PGPPublicKeyRing pgpPublicKeyRing = PGPKeyUtil.readPublicKeyRing( keyringAsASCII );

            if ( pgpPublicKeyRing != null )
            {
                savePublicKeyRing( hostId,type, pgpPublicKeyRing );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error storing Public key:" + ex.toString() );
        }
    }


    /* *****************************
     *
     */
    @Override
    public void savePublicKeyRing( String hostId, short type, PGPPublicKeyRing publicKeyRing)
    {
        try
        {
            PGPPublicKey publicKey = PGPKeyUtil.readPublicKey( publicKeyRing );

            if ( publicKey != null )
            {
                // Store public key in the KeyServer
                keyServer.addPublicKey( publicKeyRing );

                String fingerprint = PGPKeyUtil.getFingerprint( publicKey.getFingerprint() );
                securityManagerDAO.saveKeyIdentityData( hostId, "", fingerprint, type );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error storing Public key:" + ex.toString() );
        }
    }


    /* *****************************
     *
     */
    @Override
    public void removePublicKeyRing( String hostId )
    {
        try
        {
            if ( hostId != keyData.getManHostId() )
            {
                securityManagerDAO.removeKeyIdentityData( hostId );
            }

            //Remove from KeyStore
            //Currently not supported
        }
        catch ( Exception ex )
        {
            LOG.error( "Error removing Public key:" + ex.toString() );
        }
    }


    /* *****************************
     *
     */
    @Override
    public void removeSecretKeyRing( String hostId )
    {
        try
        {
            if ( hostId != keyData.getManHostId() )
            {
                String fingerprint = securityManagerDAO.getSecretKeyFingerprint( hostId );
                secretKeyStoreDAO.removeSecretKeyRing( fingerprint );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error removing Public key:" + ex.toString() );
        }
    }


    /* *****************************
     *
     */
    @Override
    public PGPPublicKey getPublicKey( String hostId )
    {
        PGPPublicKeyRing publicKeyRing = null;

        try
        {
            publicKeyRing = getPublicKeyRing( hostId );

            if ( publicKeyRing != null )
            {
                return PGPKeyUtil.readPublicKey( publicKeyRing );
            }
            else
            {
                LOG.error( "********* Error getting Public key ********" );
                return null;
            }
        }
        catch ( PGPException e )
        {
            return null;
        }
    }


    /* *****************************
     *
     */
    @Override
    public String getPublicKeyRingAsASCII( String hostId )
    {
        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = keyData.getManHostId();
        }

        try
        {
            String fingerprint = securityManagerDAO.getPublicKeyFingerprint( hostId );

            if ( Strings.isNullOrEmpty( fingerprint ) )
            {
                LOG.error( "Error !Public key not found :" );
                return "";
            }
            else
            {
                byte[] keyData = keyServer.getPublicKeyByFingerprint( fingerprint ).getKeyData();

                return PGPEncryptionUtil.armorByteArrayToString( keyData );
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Public keyRing:" + ex.toString() );
            return "";
        }
    }


    /* *****************************
     *
     */
    @Override
    public PGPPublicKeyRing getPublicKeyRing( String hostId )
    {
        PGPPublicKeyRing publicKeyRing = null;

        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = keyData.getManHostId();
        }

        try
        {
            String fingerprint = securityManagerDAO.getPublicKeyFingerprint( hostId );

            byte[] keyData = keyServer.getPublicKeyByFingerprint( fingerprint ).getKeyData();

            publicKeyRing = PGPKeyUtil.readPublicKeyRing( keyData );

            return publicKeyRing;
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Public key:" + ex.toString() );
            return publicKeyRing;
        }
    }


    /* *****************************
     *
     */
    @Override
    public PGPSecretKeyRing getSecretKeyRing( String hostId )
    {
        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = keyData.getManHostId();
        }

        try
        {
            PGPSecretKeyRing secretKeyRing = null;
            String fingerprint = securityManagerDAO.getSecretKeyFingerprint( hostId );
            secretKeyRing = PGPKeyUtil.readSecretKeyRing( secretKeyStoreDAO.getSecretKeyData( fingerprint ).getData() );

            if ( secretKeyRing != null )
            {
                return secretKeyRing;
            }
            else
            {
                LOG.error( "Object not found with fprint:" + fingerprint );
                return null;
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
            return null;
        }
    }

    /* *****************************
     *
     */
    @Override
    public InputStream getSecretKeyRingInputStream( String hostId )
    {
        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = keyData.getManHostId();
        }

        try
        {
            InputStream secretKeyRingStream = null;
            String fingerprint = securityManagerDAO.getSecretKeyFingerprint( hostId );
            secretKeyRingStream = PGPKeyUtil.readSecretKeyRingInputStream( secretKeyStoreDAO.getSecretKeyData( fingerprint ).getData() );

            if ( secretKeyRingStream != null )
            {
                return secretKeyRingStream;
            }
            else
            {
                LOG.error( "Object not found with fprint:" + fingerprint );
                return null;
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
            return null;
        }
    }

    /* *************************************************************
     *
     */
    @Override
    public PGPSecretKey getSecretKey( String hostId )
    {
        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = keyData.getManHostId();
        }

        try
        {
            PGPSecretKeyRing secretKeyRing = getSecretKeyRing( hostId );

            if ( secretKeyRing != null )
            {
                return secretKeyRing.getSecretKey();
            }
            else
            {
                return null;
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
            return null;
        }
    }


    /* ******************************************************
     *
     */
    @Override
    public PGPPrivateKey getPrivateKey( String hostId )
    {
        PGPPrivateKey privateKey = null;

        if ( Strings.isNullOrEmpty( hostId ) )
        {
            hostId = keyData.getManHostId();
        }

        try
        {
            PGPSecretKey secretKey = getSecretKey( hostId );

            if ( secretKey != null )
            {
                privateKey = PGPEncryptionUtil.getPrivateKey( secretKey, keyData.getSecretKeyringPwd() );

                return privateKey;
            }
            else
            {
                return null;
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Private key:" + ex.toString() );
            return privateKey;
        }
    }


    /* ******************************************************************
     *
     */
    @Override
    public PGPSecretKey getSecretKeyByFingerprint( String fingerprint )
    {
        PGPSecretKey secretKey = null;

        try
        {
            ByteArrayInputStream barIn =
                    new ByteArrayInputStream( secretKeyStoreDAO.getSecretKeyData( fingerprint ).getData() );

            secretKey = PGPEncryptionUtil.findSecretKeyByFingerprint( barIn, fingerprint );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error getting Secret key:" + ex.toString() );
        }

        return secretKey;
    }


    /* *****************************
     *
     */
    public SecurityKeyData getSecurityKeyData()
    {
        return keyData;
    }


    /* *****************************************
     *
     */
    @Override
    public KeyPair generateKeyPair( String userId, boolean armored )
    {
        KeyPair keyPair = null;

        try
        {
            keyPair = PGPEncryptionUtil.generateKeyPair( userId, keyData.getSecretKeyringPwd(), armored );
            return keyPair;
        }
        catch ( Exception ex )
        {
            return null;
        }
    }


    /* *****************************************
     *
     */
    @Override
    public void saveKeyPair( String hostId,short type, KeyPair keyPair )
    {
        try
        {
            saveSecretKeyRing( hostId,type, PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() ) );
            savePublicKeyRing( hostId,type, PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() ) );
        }
        catch ( Exception ex )
        {

        }
    }


    /* *****************************************
     *
     */
    @Override
    public void removeKeyRings( String hostId )
    {
        try
        {
            if ( hostId != keyData.getManHostId() )
            {
                removeSecretKeyRing( hostId );
                removePublicKeyRing( hostId );
            }
            else
            {
                LOG.error( hostId + " Cannot be removed (possibly ManagementHost):" );
            }
        }
        catch ( Exception ex )
        {

        }
    }
}
