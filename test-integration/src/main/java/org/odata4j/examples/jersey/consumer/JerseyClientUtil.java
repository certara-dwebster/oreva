package org.odata4j.examples.jersey.consumer;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.ClientConfig;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.examples.jersey.consumer.behaviors.JerseyClientBehavior;
import org.odata4j.urlencoder.ConversionUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JerseyClientUtil {

  public static Client newClient(JerseyClientFactory clientFactory, OClientBehavior[] behaviors) {
    ClientConfig cc = new ClientConfig();
    if (behaviors != null) {
      for (OClientBehavior behavior : behaviors)
      {
        if (behavior instanceof JerseyClientBehavior) {
          ((JerseyClientBehavior) behavior).modify(cc);
        }
      }
    }
    Client client = clientFactory.createClient(cc);
    if (behaviors != null)
    {
      for (OClientBehavior behavior : behaviors)
      {
        if (behavior instanceof JerseyClientBehavior) {
          ((JerseyClientBehavior) behavior).modifyClientFilters(client);
        }
      }
    }
    return client;
  }

  public static WebTarget resource(Client client, String url, OClientBehavior[] behaviors) {
    WebTarget resource = client.target(encodeURl(url));
    if (behaviors != null)
    {
      for (OClientBehavior behavior : behaviors)
      {
        if (behavior instanceof JerseyClientBehavior) {
          ((JerseyClientBehavior) behavior).modifyWebResourceFilters(resource);
        }
      }
    }
    return resource;
  }
  
  /**
   * 
   * =================================================================================================================
   * Fix bug Bug 171678 - query with primary keys hangs
   * example primary keys: 
   * (pick_interpreter='SIM2',pick_name='100 (Top Stage IVF)',pick_obs_no=1,unique_wellbore_identifier='040292797601')
   * The issues in the original code are:
   * (1) matcher.find() takes very long time because Regular expression is invalid 
   * (2) If it does find the matches in some caes, it only returns part of primary key value "(xxx)" 
   *     and will miss URL encoding special characters such as spaces before and after "(Top Stage IVF)"
   * 
   * The fix is use RegEx "\\(([^(]+|(?))?\\)"  and "\\(([^)]+|(?))?\\)"
   * =============================================================================================
   * Original code:
   * Regular expression '\\(([^)(]+|(?))+\\)' matches the string within the bracket in the URL.
   * ================================================================================================
   * String in the bracket are OEntityKey which contain special character,
   * here we are encoding the key in URL.
   * 
   * @param url the url
   * @return the string
   */
  public static String encodeURl(String url) {

    Pattern pattern1 = Pattern.compile("\\((.*)\\)");
    Matcher matcher1 = pattern1.matcher(url);
    while (matcher1.find()) {
      String st = url.substring(matcher1.start(), matcher1.end());
      url = url.replace(st, ConversionUtil.encodeString(st));
    }

    return url;
  }
}
