package com.hp.autonomy.frontend.configuration;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.hp.autonomy.frontend.configuration.CasConfig;
import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.configuration.UsernameAndPassword;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.autonomy.nonaci.indexing.IndexingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class LoginTest {
	
	private Login login;

    @Before
    public void setUp() {
        this.buildLogin("cas", this.createValidCAS(), this.createValidCommunity(), this.createValidUsernameAndPassword());
    }
    
    private CasConfig createValidCAS(){
    	final CasConfig.Builder casBuilder = new CasConfig.Builder();
    	casBuilder.setCasServerLoginUrl("http://some.server");
        casBuilder.setCasServerUrlPrefix("ciccio");
        casBuilder.setServerName("name");
        return casBuilder.build();
    }
    
    private CasConfig createCustomCAS(final String loginUrl, final String urlPrefix, final String serverName){
    	final CasConfig.Builder casBuilder = new CasConfig.Builder();
    	casBuilder.setCasServerLoginUrl(loginUrl);
        casBuilder.setCasServerUrlPrefix(urlPrefix);
        casBuilder.setServerName(serverName);
        return casBuilder.build();
    }
    
    private ServerConfig createValidCommunity(){
    	return createCustomCommunity("http://ciccio.com", 666, AciServerDetails.TransportProtocol.HTTP);
    }
    
    private ServerConfig createCustomCommunity(final String host, final int port, final AciServerDetails.TransportProtocol protocol){
        return new ServerConfig.Builder()
            .setHost(host)
            .setPort(port)
            .setProtocol(protocol)
            .build();
    }
    
    private UsernameAndPassword createValidUsernameAndPassword(){
    	return new UsernameAndPassword("Angus", "Young");
    }
    
    private void buildLogin(final String method, final CasConfig cas, final ServerConfig community, final UsernameAndPassword uap){
    	login = new Login.Builder()
            .setMethod(method)
            .setCas(cas)
            .setCommunity(community)
            .setDefaultLogin(uap).build();
    }

	@Test(expected=ConfigException.class)
    public void testValidateFailWithEmptyCasFields() throws ConfigException {
		this.buildLogin("cas", this.createCustomCAS("", "", ""), this.createValidCommunity(),
            this.createValidUsernameAndPassword());
		login.basicValidate();
    }
    
    @Test(expected=ConfigException.class)
    public void testValidateFailWithNullCasFields() throws ConfigException {
    	this.buildLogin("cas", this.createCustomCAS(null, null, null), this.createValidCommunity(),
            this.createValidUsernameAndPassword());
		login.basicValidate();
    }
    
    @Test(expected=ConfigException.class)
    public void testValidateFailWithEmptyCommunityFields() throws ConfigException {
    	this.buildLogin("community", this.createValidCAS(), this.createCustomCommunity("", 1234, AciServerDetails.TransportProtocol.HTTP),
            this.createValidUsernameAndPassword());
		login.basicValidate();
    }
    
    @Test(expected=ConfigException.class)
    public void testValidateFailWithNullProtocolCommunityFields() throws ConfigException {
    	this.buildLogin("community", this.createValidCAS(), this.createCustomCommunity("", 1234, null),
            this.createValidUsernameAndPassword());
		login.basicValidate();
    }
    
    @Test(expected=ConfigException.class)
    public void testValidateFailWithNullCommunityFields() throws ConfigException {
    	this.buildLogin("autonomy", this.createValidCAS(), this.createCustomCommunity(null, 1234, AciServerDetails.TransportProtocol.HTTP),
            this.createValidUsernameAndPassword());
		login.basicValidate();
    }
    
    @Test
    public void testValidatePass() throws ConfigException {
    	login.basicValidate();
    }

    @Test
    public void testValidateWithValidCommunity() {
        final ServerConfig community = mock(ServerConfig.class);
        Mockito.<ValidationResult<?>>when(community.validate(any(AciService.class), any(IndexingService.class))).thenReturn(new ValidationResult<>(true));

        final Login login = new Login.Builder().setMethod("autonomy").setCommunity(community).build();
        assertTrue(login.validate(null).isValid());
    }

    @Test
    public void testValidateWithInvalidCommunity() {
        final ServerConfig community = mock(ServerConfig.class);
        Mockito.<ValidationResult<?>>when(community.validate(any(AciService.class), any(IndexingService.class))).thenReturn(new ValidationResult<>(false));

        final Login login = new Login.Builder().setMethod("autonomy").setCommunity(community).build();
        assertFalse(login.validate(null).isValid());
    }
}
