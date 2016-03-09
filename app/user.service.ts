import {Injectable}     from 'angular2/core';
import {Http, Response, Headers, RequestOptions} from 'angular2/http';
import {Observable}     from 'rxjs/Observable';

import {User} from './user';
import {Request} from "angular2/http";
import {Json} from "angular2/src/facade/lang";

@Injectable()
export class UserService
{
  constructor(private http:Http)
  {
    console.log("creating new UserService: " + http);
  }

  private _createUrl = 'http://localhost:8080/createUser';
  private _loginUrl = 'http://localhost:8080/login';

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
    let options = new RequestOptions({headers: headers, withCredentials: true});

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
