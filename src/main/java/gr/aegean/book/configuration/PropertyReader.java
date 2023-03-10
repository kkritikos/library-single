package gr.aegean.book.configuration;

import java.io.InputStream;
import java.util.Properties;

public final class PropertyReader {
    private static String dbName;
    private static String dbHost;
    private static String dbPort;
    private static String login;
    private static String pwd;

    private PropertyReader() {}
	
    static {
    	getProperties();
    }
	 
    private static Properties loadPropertyFile()
    {
    	try{
    			InputStream input = PropertyReader.class.getClassLoader().getResourceAsStream("service.properties");
    			Properties prop = new Properties(); 
    			if (input == null) { 
    				System.out.println("Sorry, unable to find service.properties"); 	
    				return null; 
    			}
    			prop.load(input); 
    			return prop;
    	} 
    	catch (Exception ex) { 
    		ex.printStackTrace();
    	}
    	return null;
    }
    
    private static String getDefaultValueIfNull(final String s, final String defaultVal) {
    	if (s == null || s.trim().equals("")) return defaultVal;
    	else return s;
    }

	private static void getProperties(){
		Properties props = loadPropertyFile();
		if (props != null){
			dbName = getDefaultValueIfNull(props.getProperty("dbName"),"");
			dbHost = getDefaultValueIfNull(props.getProperty("dbHost"),"");
			dbPort = getDefaultValueIfNull(props.getProperty("dbPort"),"");
			login = getDefaultValueIfNull(props.getProperty("login"),"");
			pwd = getDefaultValueIfNull(props.getProperty("pwd"),"");
		}
		try {
			String altDbHost = System.getenv("DB_HOST");
			if (altDbHost != null && !altDbHost.equals("")) dbHost = altDbHost; 
		}
		catch(Exception e) {}
		
		String loginVar = System.getProperty("DB_USER");
		if (loginVar != null) login = loginVar;
		
		String pwdVar = System.getProperty("DB_PWD");
		if (pwdVar != null) pwd = pwdVar;
	}
	
	public static String getDbName() {
		return dbName;
	}
	
	public static String getDbHost() {
		return dbHost;
	}
	
	public static void setDbHost(String dbHost) {
		PropertyReader.dbHost = dbHost;
	}
	
	public static String getDbPort() {
		return dbPort;
	}

	public static String getLogin() {
		return login;
	}
	
	public static String getPwd() {
		return pwd;
	}
}
