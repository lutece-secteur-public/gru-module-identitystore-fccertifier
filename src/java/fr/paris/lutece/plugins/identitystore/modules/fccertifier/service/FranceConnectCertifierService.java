/*
 * Copyright (c) 2002-2017, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.identitystore.modules.fccertifier.service;

import fr.paris.lutece.plugins.franceconnect.oidc.UserInfo;
import fr.paris.lutece.plugins.grubusiness.business.customer.Customer;
import fr.paris.lutece.plugins.grubusiness.business.demand.Demand;
import fr.paris.lutece.plugins.grubusiness.business.notification.BackofficeNotification;
import fr.paris.lutece.plugins.grubusiness.business.notification.BroadcastNotification;
import fr.paris.lutece.plugins.grubusiness.business.notification.Notification;
import fr.paris.lutece.plugins.grubusiness.business.notification.MyDashboardNotification;
import fr.paris.lutece.plugins.grubusiness.business.notification.EmailAddress;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifier;
import fr.paris.lutece.plugins.identitystore.business.AttributeCertifierHome;
import fr.paris.lutece.plugins.identitystore.business.Identity;
import fr.paris.lutece.plugins.identitystore.business.IdentityHome;
import fr.paris.lutece.plugins.identitystore.modules.fccertifier.business.FcIdentity;
import fr.paris.lutece.plugins.identitystore.service.ChangeAuthor;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.web.service.AuthorType;
import fr.paris.lutece.plugins.librarynotifygru.services.NotificationService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.sql.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * FranceConnect Certifier Service
 */
public class FranceConnectCertifierService
{
    private static final String MESSAGE_CODE_VALIDATION_OK = "module.identitystore.fccertifier.message.validation.ok";
    private static final String MESSAGE_CODE_VALIDATION_INVALID = "module.identitystore.fccertifier.message.validation.invalidCode";
    private static final String MESSAGE_SESSION_EXPIRED = "module.identitystore.fccertifier.message.validation.sessionExpired";
    private static final String MESSAGE_CODE_EXPIRED = "module.identitystore.fccertifier.message.validation.codeExpired";
    private static final String MESSAGE_TOO_MANY_ATTEMPS = "module.identitystore.fccertifier.message.validation.tooManyAttempts";
     private static final String MESSAGE_SMS_VALIDATION_CONFIRM_TEXT = "module.identitystore.fccertifier.message.validation.smsValidationConfirmText";
    private static final String PROPERTY_CODE_LENGTH = "identitystore.fccertifier.codeLength";
    private static final String PROPERTY_EXPIRES_DELAY = "identitystore.fccertifier.expiresDelay";
    private static final String PROPERTY_MAX_ATTEMPTS = "identitystore.fccertifier.maxAttempts";
    private static final String PROPERTY_MOCKED_EMAIL = "identitystore.fccertifier.mockedEmail";
    private static final String PROPERTY_MOCKED_CONNECTION_ID = "identitystore.fccertifier.mockedConnectionId";
    private static final String PROPERTY_API_MANAGER_ENABLED = "identitystore.fccertifier.apiManager.enabled";
    private static final String PROPERTY_CERTIFIER_CODE = "identitystore.fccertifier.certifierCode";
    private static final String PROPERTY_CERTIFIER_CLOSE_CRM_STATUS_ID = "identitystore.fccertifier.crmCloseStatusId";
    private static final String PROPERTY_CERTIFIER_CLOSE_DEMAND_STATUS_ID = "identitystore.fccertifier.demandCloseStatusId";
    private static final String PROPERTY_CERTIFIER_DEMAND_TYPE_ID = "identitystore.fccertifier.demandTypeId";
    private static final String PROPERTY_GRU_NOTIF_EMAIL_SENDER_MAIL = "identitystore.fccertifier.senderMail";
    private static final String PROPERTY_GRU_NOTIF_EMAIL_SENDER_NAME = "identitystore.fccertifier.senderName";
    private static final String PROPERTY_CERTIFICATE_LEVEL = "identitystore.fccertifier.certificate.level";
    private static final String PROPERTY_CERTIFICATE_EXPIRATION_DELAY = "identitystore.fccertifier.certificate.expirationDelay";
    private static final String MESSAGE_GRU_NOTIF_DASHBOARD_STATUS_TEXT = "module.identitystore.fccertifier.gru.notif.dashboard.statusText";
    private static final String MESSAGE_GRU_NOTIF_DASHBOARD_MESSAGE = "module.identitystore.fccertifier.gru.notif.dashboard.message";
    private static final String MESSAGE_GRU_NOTIF_DASHBOARD_SUBJECT = "module.identitystore.fccertifier.gru.notif.dashboard.subject";
    private static final String MESSAGE_GRU_NOTIF_DASHBOARD_DATA = "module.identitystore.fccertifier.gru.notif.dashboard.data";
    private static final String MESSAGE_GRU_NOTIF_DASHBOARD_SENDER_NAME = "module.identitystore.fccertifier.gru.notif.dashboard.senderName";
    private static final String MESSAGE_GRU_NOTIF_EMAIL_SUBJECT = "module.identitystore.fccertifier.gru.notif.email.subject";
    private static final String MESSAGE_GRU_NOTIF_EMAIL_MESSAGE = "module.identitystore.fccertifier.gru.notif.email.message";
    private static final String MESSAGE_GRU_NOTIF_AGENT_MESSAGE = "module.identitystore.fccertifier.gru.notif.agent.message";
    private static final String MESSAGE_GRU_NOTIF_AGENT_STATUS_TEXT = "module.identitystore.fccertifier.gru.notif.agent.statusText";
    private static final String DEFAULT_CERTIFIER_CODE = "fccertifier";
    private static final String DEFAULT_CONNECTION_ID = "1";
    private static final String DEFAULT_EMAIL = "test@test.fr";
    private static final int DEFAULT_CERTIFIER_CRM_CLOSE_STATUS_ID = 1;
    private static final int DEFAULT_CERTIFIER_DEMAND_CLOSE_STATUS_ID = 1;
    private static final String DEFAULT_CERTIFIER_DEMAND_TYPE_ID = "401";
    private static final int DEFAULT_LENGTH = 6;
    private static final int DEFAULT_EXPIRES_DELAY = 5;
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final int NO_CERTIFICATE_EXPIRATION_DELAY = -1;
    private static final int DEFAULT_CERTIFICATE_LEVEL = 1;
    private static final String CERTIFIER_CODE = AppPropertiesService.getProperty( PROPERTY_CERTIFIER_CODE,
            DEFAULT_CERTIFIER_CODE );
    private static final String MOCKED_USER_CONNECTION_ID = AppPropertiesService.getProperty( PROPERTY_MOCKED_CONNECTION_ID,
            DEFAULT_CONNECTION_ID );
    private static final String MOCKED_USER_EMAIL = AppPropertiesService.getProperty( PROPERTY_MOCKED_EMAIL,
            DEFAULT_EMAIL );
    private static final int EXPIRES_DELAY = AppPropertiesService.getPropertyInt( PROPERTY_EXPIRES_DELAY,
            DEFAULT_EXPIRES_DELAY );
    private static final int CODE_LENGTH = AppPropertiesService.getPropertyInt( PROPERTY_CODE_LENGTH, DEFAULT_LENGTH );
    private static final int MAX_ATTEMPTS = AppPropertiesService.getPropertyInt( PROPERTY_MAX_ATTEMPTS,
            DEFAULT_MAX_ATTEMPTS );
    private static final int CERTIFICATE_EXPIRATION_DELAY = AppPropertiesService.getPropertyInt( PROPERTY_CERTIFICATE_EXPIRATION_DELAY,
            NO_CERTIFICATE_EXPIRATION_DELAY );
    private static final int CERTIFICATE_LEVEL = AppPropertiesService.getPropertyInt( PROPERTY_CERTIFICATE_LEVEL,
            DEFAULT_CERTIFICATE_LEVEL );
    private static final String SERVICE_NAME = "France Connect Certifier Service";
    private static final String DEMAND_PREFIX = "MOBCERT_";
    private static final String BEAN_NOTIFICATION_SENDER = "identitystore-fccertifier.lib-notifygru.notificationService";
    
    
    private static Map<String, ValidationInfos> _mapValidationCodes = new HashMap<String, ValidationInfos>(  );
    
    private NotificationService _notifyGruSenderService;

    /**
     * constructor
     */
    public FranceConnectCertifierService(  )
    {
        super(  );
        _notifyGruSenderService = SpringContextService.getBean( BEAN_NOTIFICATION_SENDER );
    }

    /**
     * Starts the validation process by generating and sending a validation code
     *
     * @param request
     *          The HTTP request
     * @throws fr.paris.lutece.portal.service.security.UserNotSignedException
     *           if no user found
     */
    public void startValidation( HttpServletRequest request )
        throws UserNotSignedException
    {

        HttpSession session = request.getSession( true );
        ValidationInfos infos = new ValidationInfos(  );
        infos.setExpiresTime( getExpiresTime(  ) );
        infos.setUserConnectionId( getUserConnectionId( request ) );
        infos.setUserEmail( getUserEmail( request ) );

        _mapValidationCodes.put( session.getId(  ), infos );
    }

    /**
     * Validate a validation code
     *
     * @param request
     *          The request
     * @param userInfo
     *          UserInfo from FranceConnect
     * @return A validation result
     */
    public ValidationResult validate( HttpServletRequest request, UserInfo userInfo )
    {
        HttpSession session = request.getSession(  );

        if ( session == null )
        {
            return ValidationResult.SESSION_EXPIRED;
        }

        String strKey = session.getId(  );
        ValidationInfos infos = _mapValidationCodes.get( strKey );

        if ( infos == null )
        {
            return ValidationResult.SESSION_EXPIRED;
        }

        if ( infos.getInvalidAttempts(  ) > MAX_ATTEMPTS )
        {
            return ValidationResult.TOO_MANY_ATTEMPS;
        }

        if ( infos.getExpiresTime(  ) < now(  ) )
        {
            _mapValidationCodes.remove( strKey );

            return ValidationResult.CODE_EXPIRED;
        }

        _mapValidationCodes.remove( strKey );
        
        infos.setFCUserInfo( new FcIdentity( userInfo ));
        
        certify( infos, request.getLocale(  ) );

        return ValidationResult.OK;
    }

    /**
     * Certify the attribute change
     *
     * @param infos
     *          The validation infos
     * @param locale
     *          the locale
     */
    private void certify( ValidationInfos infos, Locale locale )
    {
        AttributeCertifier certifier = AttributeCertifierHome.findByCode( CERTIFIER_CODE );
        AttributeCertificate certificate = new AttributeCertificate(  );
        certificate.setCertificateDate( new Timestamp( new Date(  ).getTime(  ) ) );
        certificate.setCertificateLevel( CERTIFICATE_LEVEL );
        certificate.setIdCertifier( certifier.getId(  ) );
        certificate.setCertifier( certifier.getName(  ) );

        if ( CERTIFICATE_EXPIRATION_DELAY != NO_CERTIFICATE_EXPIRATION_DELAY )
        {
            Calendar c = Calendar.getInstance(  );
            c.setTime( new Date(  ) );
            c.add( Calendar.DATE, CERTIFICATE_EXPIRATION_DELAY );
            certificate.setExpirationDate( new Timestamp( c.getTime(  ).getTime(  ) ) );
        }

        ChangeAuthor author = new ChangeAuthor(  );
        author.setApplication( SERVICE_NAME );
        author.setType( AuthorType.TYPE_USER_OWNER.getTypeValue(  ) );

        Identity identity = IdentityHome.findByConnectionId( infos.getUserConnectionId(  ) );
        IdentityStoreService.setAttribute( identity, "family_name", infos.getFCUserInfo().getFamilyName(), author, certificate );
        IdentityStoreService.setAttribute( identity, "birthplace", infos.getFCUserInfo().getIdsBirthPlace(), author, certificate );
        IdentityStoreService.setAttribute( identity, "birthdate", infos.getFCUserInfo().getIdsBirthDate(), author, certificate );
        IdentityStoreService.setAttribute( identity, "birthcountry", infos.getFCUserInfo().getIdsBirthCountry(), author, certificate );
        IdentityStoreService.setAttribute( identity, "fc_gender", infos.getFCUserInfo().getGender(), author, certificate );
        IdentityStoreService.setAttribute( identity, "fc_family_name", infos.getFCUserInfo().getFamilyName(), author, certificate );
        IdentityStoreService.setAttribute( identity, "fc_birthplace", infos.getFCUserInfo().getBirthPlace(), author, certificate );
        IdentityStoreService.setAttribute( identity, "fc_birthdate", infos.getFCUserInfo().getBirthDate(), author, certificate );
        IdentityStoreService.setAttribute( identity, "fc_given_name", infos.getFCUserInfo().getGivenName(), author, certificate );
        IdentityStoreService.setAttribute( identity, "fc_birthcountry", infos.getFCUserInfo().getBirthCountry(), author, certificate );

        if ( AppPropertiesService.getPropertyBoolean( PROPERTY_API_MANAGER_ENABLED, true ) )
        {
            Notification certifNotif = buildCertifiedNotif( infos, locale );

            _notifyGruSenderService.send( certifNotif );
        }
        else
        {
            // mock mode => certification message is logged
            AppLogService.info( I18nService.getLocalizedString( MESSAGE_SMS_VALIDATION_CONFIRM_TEXT, locale ) );
        }
    }

    /**
     * build a notification from validation infos
     *
     * @param infos
     *          validations infos
     * @param locale
     *          locale
     * @return Notification notification to send (SMS, agent,
     *         dashboard, email)
     */
    private static Notification buildCertifiedNotif( ValidationInfos infos, Locale locale )
    {
        Notification certifNotif = new Notification(  );
        certifNotif.setDate( new Date(  ).getTime(  ) );

        Demand demand = new Demand(  );
        demand.setId( generateDemandId(  ) );
        demand.setReference( DEMAND_PREFIX + demand.getId(  ) );
        demand.setStatusId( AppPropertiesService.getPropertyInt( PROPERTY_CERTIFIER_CLOSE_DEMAND_STATUS_ID,
                DEFAULT_CERTIFIER_DEMAND_CLOSE_STATUS_ID ) );
        demand.setTypeId( AppPropertiesService.getProperty( PROPERTY_CERTIFIER_DEMAND_TYPE_ID,
                DEFAULT_CERTIFIER_DEMAND_TYPE_ID ) );

        Customer customer = new Customer(  );
        customer.setConnectionId( infos.getUserConnectionId(  ) );
        customer.setEmail( infos.getUserEmail(  ) );
        demand.setCustomer( customer );

        certifNotif.setDemand( demand );


        MyDashboardNotification notifDashboard = new MyDashboardNotification(  );
        notifDashboard.setStatusId( AppPropertiesService.getPropertyInt( 

                PROPERTY_CERTIFIER_CLOSE_CRM_STATUS_ID, DEFAULT_CERTIFIER_CRM_CLOSE_STATUS_ID )  );

        notifDashboard.setSubject( I18nService.getLocalizedString( MESSAGE_GRU_NOTIF_DASHBOARD_SUBJECT, locale ) );
        notifDashboard.setMessage( I18nService.getLocalizedString( MESSAGE_GRU_NOTIF_DASHBOARD_MESSAGE, locale ) );
        notifDashboard.setStatusText( I18nService.getLocalizedString( MESSAGE_GRU_NOTIF_DASHBOARD_STATUS_TEXT, locale ) );
        notifDashboard.setSenderName( I18nService.getLocalizedString( MESSAGE_GRU_NOTIF_DASHBOARD_SENDER_NAME, locale ) );
        notifDashboard.setData( I18nService.getLocalizedString( MESSAGE_GRU_NOTIF_DASHBOARD_DATA, locale ) );
        certifNotif.setMyDashboardNotification( notifDashboard );


        BroadcastNotification broadcastEmail = new BroadcastNotification(  );
        broadcastEmail.setMessage( I18nService.getLocalizedString( MESSAGE_GRU_NOTIF_EMAIL_MESSAGE, locale ) );
        broadcastEmail.setSubject( I18nService.getLocalizedString( MESSAGE_GRU_NOTIF_EMAIL_SUBJECT, locale ) );
        broadcastEmail.setSenderEmail( AppPropertiesService.getProperty( PROPERTY_GRU_NOTIF_EMAIL_SENDER_MAIL ) );
        broadcastEmail.setSenderName( AppPropertiesService.getProperty( PROPERTY_GRU_NOTIF_EMAIL_SENDER_NAME ) );

        broadcastEmail.setRecipient( EmailAddress.buildEmailAddresses( new String[] { infos.getUserEmail(  ) } ) );

        certifNotif.addBroadcastEmail( broadcastEmail );

        BackofficeNotification notifAgent = new BackofficeNotification(  );
        notifAgent.setMessage( I18nService.getLocalizedString( MESSAGE_GRU_NOTIF_AGENT_MESSAGE,locale ) );
        notifAgent.setStatusText( I18nService.getLocalizedString( MESSAGE_GRU_NOTIF_AGENT_STATUS_TEXT, locale ) );
        certifNotif.setBackofficeNotification( notifAgent );

        return certifNotif;
    }

    /**
     * generate demandid for sms certification
     *
     * @return demand id
     */
    private static String generateDemandId(  )
    {
        // FIXME =>how to generate a unique id
        Random rand = new Random(  );
        int randomNum = rand.nextInt(  );

        return String.valueOf( Math.abs( randomNum ) );
    }

    /**
     * returns the user connection ID
     *
     * @param request
     *          The HTTP request
     * @return the user connection ID
     * @throws UserNotSignedException
     *           If no user is connected
     */
    private static String getUserConnectionId( HttpServletRequest request )
        throws UserNotSignedException
    {
        if ( SecurityService.isAuthenticationEnable(  ) )
        {
            LuteceUser user = SecurityService.getInstance(  ).getRegisteredUser( request );

            if ( user != null )
            {
                return user.getName(  );
            }
            else
            {
                throw new UserNotSignedException(  );
            }
        }
        else
        {
            return MOCKED_USER_CONNECTION_ID;
        }
    }

    /**
     * returns the user email
     *
     * @param request
     *          The HTTP request
     * @return the user connection ID
     * @throws UserNotSignedException
     *           If no user is connected
     */
    private static String getUserEmail( HttpServletRequest request )
        throws UserNotSignedException
    {
        if ( SecurityService.isAuthenticationEnable(  ) )
        {
            LuteceUser user = SecurityService.getInstance(  ).getRegisteredUser( request );

            if ( user != null )
            {
                return user.getEmail(  );
            }
            else
            {
                throw new UserNotSignedException(  );
            }
        }
        else
        {
            return MOCKED_USER_EMAIL;
        }
    }

    /**
     * Calculate an expiration time
     *
     * @return the time as a long value
     */
    private static long getExpiresTime(  )
    {
        return now(  ) + ( (long) EXPIRES_DELAY * 60000L );
    }

    /**
     * The current time as a long value
     *
     * @return current time as a long value
     */
    private static long now(  )
    {
        return ( new Date(  ) ).getTime(  );
    }

    /**
     * Enumeration of all validation results
     */
    public enum ValidationResult
    {
        OK( MESSAGE_CODE_VALIDATION_OK ),
        INVALID_CODE( MESSAGE_CODE_VALIDATION_INVALID ),
        SESSION_EXPIRED( MESSAGE_SESSION_EXPIRED ),
        CODE_EXPIRED( MESSAGE_CODE_EXPIRED ),
        TOO_MANY_ATTEMPS( MESSAGE_TOO_MANY_ATTEMPS );	    	
    	
    	private String _strMessageKey;

        /**
         * Constructor
         *
         * @param strMessageKey
         *          The i18n message key
         */
        ValidationResult( String strMessageKey )
        {
            _strMessageKey = strMessageKey;
        }

        /**
         * Return the i18n message key
         *
         * @return the i18n message key
         */
        public String getMessageKey(  )
        {
            return _strMessageKey;
        }

    }
}
