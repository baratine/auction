package examples.auction;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class PayPalAuth
{
  private String _token;
  private String _auth;

  public PayPalAuth(String response)
  {
    _auth = _auth;

    JsonReader reader = Json.createReader(new StringReader(response));
    JsonObject json = reader.readObject();

    String token = json.getString("access_token");

    _token = token;
  }

  public String getToken()
  {
    return _token;
  }
}
