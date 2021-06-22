package fr.paris.lutece.plugins.identitystore.modules.fccertifier.service.certifier;

import fr.paris.lutece.plugins.identitystore.service.certifier.IGenerateAutomaticCertifierAttribute;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.fckey.FCKeyException;
import fr.paris.lutece.util.fckey.FCKeyService;

/**
 * The Class AutomaticFcKeyGenerator.
 */
public class AutomaticFcKeyGenerator implements IGenerateAutomaticCertifierAttribute {

	
	private static final String FIELD_SEPARATOR="|";
	private static final String CRYPTO_ALGO ="SHA-256";
	
	/** The str gender attribute. */
	private String _strGenderAttribute;

	/** The str first name attribute. */
	private String _strFirstNameAttribute;

	/** The str family name attribute. */
	private String _strFamilyNameAttribute;

	/** The str birthplace attribute. */
	private String _strBirthplaceAttribute;

	/** The str birthdate attribute. */
	private String _strBirthdateAttribute;

	/** The str birthcountry attribute. */
	private String _strBirthcountryAttribute;

	/**
	 * Gets the gender attribute.
	 *
	 * @return the gender attribute
	 */
	public String getGenderAttribute() {
		return _strGenderAttribute;
	}

	/**
	 * Sets the gender attribute.
	 *
	 * @param _strGenderAttribute the new gender attribute
	 */
	public void setGenderAttribute(String _strGenderAttribute) {
		this._strGenderAttribute = _strGenderAttribute;
	}

	/**
	 * Sets the first name attribute.
	 *
	 * @param _strFirstNameAttribute the new first name attribute
	 */
	public void setFirstNameAttribute(String _strFirstNameAttribute) {
		this._strFirstNameAttribute = _strFirstNameAttribute;
	}

	/**
	 * Sets the family name attribute.
	 *
	 * @param _strFamilyNameAttribute the new family name attribute
	 */
	public void setFamilyNameAttribute(String _strFamilyNameAttribute) {
		this._strFamilyNameAttribute = _strFamilyNameAttribute;
	}

	/**
	 * Sets the birthplace attribute.
	 *
	 * @param _strBirthplaceAttribute the new birthplace attribute
	 */
	public void setBirthplaceAttribute(String _strBirthplaceAttribute) {
		this._strBirthplaceAttribute = _strBirthplaceAttribute;
	}

	/**
	 * Sets the birthdate attribute.
	 *
	 * @param _strBirthdateAttribute the new birthdate attribute
	 */
	public void setBirthdateAttribute(String _strBirthdateAttribute) {
		this._strBirthdateAttribute = _strBirthdateAttribute;
	}

	/**
	 * Sets the birthcountry attribute.
	 *
	 * @param _strBirthcountryAttribute the new birthcountry attribute
	 */
	public void setBirthcountryAttribute(String _strBirthcountryAttribute) {
		this._strBirthcountryAttribute = _strBirthcountryAttribute;
	}
	
	
	
	/**
     * {@inheritDoc}
     */
    @Override
	public boolean mustBeGenerated(IdentityDto identityDTO, String strCertifierCode) {

		if (containsAttributeCertificated(identityDTO, _strGenderAttribute, strCertifierCode)
				&& containsAttributeCertificated(identityDTO, _strFirstNameAttribute, strCertifierCode)
				&& containsAttributeCertificated(identityDTO, _strFamilyNameAttribute, strCertifierCode)
				&& containsAttributeCertificated(identityDTO, _strBirthplaceAttribute, strCertifierCode)
				&& containsAttributeCertificated(identityDTO, _strBirthcountryAttribute, strCertifierCode)
				&& containsAttributeCertificated(identityDTO, _strBirthdateAttribute, strCertifierCode)) {

			try {
				FCKeyService.validateKeyInformations(identityDTO.getAttributes().get(_strGenderAttribute).getValue(),
						identityDTO.getAttributes().get(_strFirstNameAttribute).getValue(),
						identityDTO.getAttributes().get(_strFamilyNameAttribute).getValue(),
						identityDTO.getAttributes().get(_strBirthplaceAttribute).getValue(),
						identityDTO.getAttributes().get(_strBirthcountryAttribute).getValue(),
						identityDTO.getAttributes().get(_strBirthdateAttribute).getValue());
				return true;
			} catch (FCKeyException e) {
				AppLogService.error("the FC key can not be generated for customer id "+ identityDTO.getCustomerId(), e);
			}

		}
		return false;

	}


    /**
     * {@inheritDoc}
     */
    @Override
	public String getValue(IdentityDto identityDTO) {

		
		String strFcKey = "";
		try {
		    strFcKey=FCKeyService.getKey(identityDTO.getAttributes().get(_strGenderAttribute).getValue(),
					identityDTO.getAttributes().get(_strFirstNameAttribute).getValue(),
					identityDTO.getAttributes().get(_strFamilyNameAttribute).getValue(),
					identityDTO.getAttributes().get(_strBirthplaceAttribute).getValue(),
					identityDTO.getAttributes().get(_strBirthcountryAttribute).getValue(),
					identityDTO.getAttributes().get(_strBirthdateAttribute).getValue());

		} catch (FCKeyException e) {
			AppLogService.error("An error appear during FC key generation for customer id "+identityDTO.getCustomerId(),e);
		}

		return strFcKey;

		

		

	}

	
	
	
	private boolean containsAttributeCertificated(IdentityDto identityDto, String strAtrributeKey,
			String strFcCertifierCode) {

		return identityDto.getAttributes().containsKey(strAtrributeKey)
				&& identityDto.getAttributes().get(strAtrributeKey).getCertificate() != null && strFcCertifierCode
						.equals(identityDto.getAttributes().get(strAtrributeKey).getCertificate().getCertifierCode());

	}

}
