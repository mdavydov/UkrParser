/*
 
 */

package org.dts.spell.swing;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/*
 * Created on Oct 11, 2003
 */

/**
 * @author Bob Tantlinger
 *
 * Static methods and a ResourceBundle for i18n of text and mnemonics
 * 
 */
public class Messages
{	
	private static final String I18N_BUNDLE = "org.dts.spell.swing.resources.messages";
    
    
    private static ResourceBundle bun = null;
    static
    {
        try
        {
            bun = ResourceBundle.getBundle(I18N_BUNDLE);
        }
        catch(MissingResourceException ex)
        {

        }
    }
    private static final ResourceBundle RESOURCE_BUNDLE = bun;

    /**
     * @param key The key value of the resource
     * @return The i18n resource
     */
    public static String getString(String key)
    {
        try
        {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            return '!' + key + '!'; 
            //just return the key if we cant find the resource
        }
    }
    

}
