package examples.auction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;

@Singleton
public class PayPalRestLink
{
  private static final Logger log
    = Logger.getLogger(PayPalRestLink.class.getName());

  private String _app;
  private String _account;
  private String _clientId;
  private String _secret;
  private String _endpoint;
  private boolean _isLoaded;

  public PayPalRestLink() throws IOException
  {
    InputStream in = null;

    try {
      in = PayPalRestLink.class.getResourceAsStream("/paypal.properties");
    } catch (Throwable t) {
      log.log(Level.WARNING, t.getMessage(), t);
    }

    if (in == null) {
      in = new FileInputStream(System.getProperty("user.home")
                               + File.separator
                               + ".paypal.properties");
    }

    try {
      Properties p = new Properties();

      p.load(in);
      _app = p.getProperty("app");
      _account = p.getProperty("account");
      _clientId = p.getProperty("client-id").trim();
      _secret = p.getProperty("secret").trim();
      _endpoint = p.getProperty("endpoint");
      _isLoaded = true;
    } catch (java.io.FileNotFoundException e) {
      log.log(Level.INFO,
              "paypal.properties is not found, PayPal will not be available");
    } finally {
      in.close();
    }
  }

  public PayPalAuth auth() throws IOException
  {
    Map<String,String> headers = new HashMap<>();

    headers.put("Authorization", "Basic " + getBA());
    headers.put("Accept", "application/json");
    headers.put("Accept-Language", "en_US");
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    String reply = send("/v1/oauth2/token",
                        "POST",
                        headers,
                        "grant_type=client_credentials".getBytes());

    return new PayPalAuth(reply);
  }

  private String getBA() throws UnsupportedEncodingException
  {
    String auth = _clientId + ':' + _secret;
    byte[] bytes = Base64.getEncoder().encode(auth.getBytes("UTF-8"));

    return new String(bytes, "UTF-8");
  }

  private String send(String subUrl,
                      String method,
                      Map<String,String> headers,
                      byte[] body)
    throws IOException
  {
    if (!subUrl.startsWith("/"))
      throw new IllegalArgumentException();

    URL url = new URL(String.format("https://%1$s", _endpoint) + subUrl);

    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);

    connection.setDoInput(true);
    connection.setDoOutput(true);

    for (Map.Entry<String,String> entry : headers.entrySet()) {
      connection.setRequestProperty(entry.getKey(), entry.getValue());
    }

    if (body != null) {
      OutputStream out = connection.getOutputStream();

      out.write(body);

      out.flush();
    }

    int responseCode = connection.getResponseCode();

    if (responseCode == 200 || responseCode == 201) {
      InputStream in = connection.getInputStream();
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] temp = new byte[1024];
      int l;

      while ((l = in.read(temp)) > 0)
        buffer.write(temp, 0, l);

      return new String(buffer.toByteArray());
    }
    else {
      log.warning(String.format("error response %1$d", responseCode));

      StringBuilder error = new StringBuilder();

      InputStream err = connection.getErrorStream();
      int i;
      while ((i = err.read()) > 0)
        error.append((char) i);

      throw new IllegalStateException(responseCode
                                      + ": "
                                      + connection.getResponseMessage()
                                      + ": "
                                      + error);
    }
  }

  /**
   * @param payPalRequestId
   * @return
   * @throws IOException
   */
  public Payment pay(String securityToken,
                     String payPalRequestId,
                     String ccNumber,
                     String ccType,
                     int ccExpireM,
                     int ccExpireY,
                     String ccv2,
                     String firstName,
                     String lastName,
                     String total,
                     String currency,
                     String description
  ) throws IOException
  {
    if (!_isLoaded)
      return null;

    final String payment;

    try (InputStream in
           = PayPalRestLink.class.getResourceAsStream("/payment.template.json")) {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();

      byte[] bytes = new byte[512];

      int l;

      while ((l = in.read(bytes)) > 0)
        buffer.write(bytes, 0, l);

      payment = String.format(new String(buffer.toByteArray(), "UTF-8"),
                              ccNumber,
                              ccType,
                              ccExpireM,
                              ccExpireY,
                              ccv2,
                              firstName,
                              lastName,
                              total,
                              currency,
                              description);

      log.finer(payment);
    }

    Map<String,String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + securityToken);
    headers.put("PayPal-Request-Id", payPalRequestId);

    String response = send("/v1/payments/payment", "POST", headers,
                           payment.getBytes("UTF-8"));

    return new PaymentImpl(response);
  }

  public String list(String token) throws IOException
  {
    Map<String,String> headers = new HashMap<>();

    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + token);

    return send("/v1/payments/payment?count=20", "GET", headers, null);
  }

  public Refund refund(String securityToken,
                       String payPalRequestId,
                       String saleId)
    throws IOException
  {
    if (!_isLoaded)
      return null;

    Map<String,String> headers = new HashMap<>();

    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + securityToken);
    headers.put("PayPal-Request-Id", payPalRequestId);

    String response = send("/v1/payments/sale/" + saleId + "/refund",
                           "POST",
                           headers,
                           "{}".getBytes(StandardCharsets.UTF_8));

    return new RefundImpl(response);
  }

  @Override
  public String toString()
  {
    return "TestLogin["
           + _app + ", "
           + _account + ", "
           + _clientId + ", "
           + _secret + ", "
           + _endpoint
           + ']';
  }
}
