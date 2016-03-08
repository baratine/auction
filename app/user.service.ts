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
  }

  private _loginUrl = 'http://localhost:8080/user/login';
  private _createUrl = 'http://localhost:8080/user/create';

  login(user:string, password:string)
  {
    let body = 'u=' + user + '&' + 'p=' + password;

    let headers = new Headers({'Content-Type': 'application/x-www-form-urlencoded'});
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._loginUrl, body, options).map(
      res=><User> res.json()).catch(this.handleError);
  }

  create(user:string, password:string)
  {
    let body = Json.stringify({"user": user, "password": password});

    let headers = new Headers({'Content-Type': 'application/json'});
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._createUrl, body, options)
      .map(res=><User> res.json()).catch(this.handleError);
  }

  private handleError(error:Response)
  {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }
}
