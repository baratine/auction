import {Injectable}     from 'angular2/core';
import {Http, Response, Headers, RequestOptions} from 'angular2/http';
import {Observable}     from 'rxjs/Observable';

import {BaseUrlProvider} from './baseurl';
import {User} from './user';
import {Request} from "angular2/http";
import {Json} from "angular2/src/facade/lang";
import {OnInit} from "angular2/core";

@Injectable()
export class UserService
{
  private _createUrl;
  private _loginUrl;

  constructor(private http:Http, private _baseUrlProvider:BaseUrlProvider)
  {
    console.log("creating new UserService: " + http);

    this._createUrl = _baseUrlProvider.baseUrl + "createUser";
    this._loginUrl = _baseUrlProvider.baseUrl + "login";
  }

  login(user:string, password:string)
  {
    let body = 'u=' + user + '&' + 'p=' + password;

    let headers = new Headers({'Content-Type': 'application/x-www-form-urlencoded'});
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._loginUrl, body, options)
      .map(res=> res.text()).catch(this.handleError);
  }

  create(user:string, password:string)
  {
    let body = Json.stringify({"user": user, "password": password});

    let headers = new Headers({'Content-Type': 'application/json'});
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._createUrl, body, options)
      .map(res=>this.map(res)).catch(this.handleError);
  }

  map(response:Response)
  {
    console.log(response);

    console.log("Set-Cookie: " + response.headers.get("Set-Cookie"));

    return response.json();
  }

  private handleError(error:Response)
  {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }
}
