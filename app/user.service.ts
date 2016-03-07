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
    let body = Json.stringify({"user": user, "password": password});

    return this.http.post(this._loginUrl, body, this.options()).map(
      res=><User> res.json()).catch(this.handleError);
  }

  create(user:string, password:string)
  {
    let body = Json.stringify({"user": user, "password": password});

    console.log(body);
    return this.http.post(this._createUrl, body, this.options()).map(
      res=><User> res.json()).catch(this.handleError);
  }

  private options()
  {
    let headers = new Headers({'Content-Type': 'application/json'});
    let options = new RequestOptions({headers: headers});

    return options;
  }

  private handleError(error:Response)
  {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }
}
