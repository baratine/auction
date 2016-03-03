import {Injectable}     from 'angular2/core';
import {Http, Response, Headers, RequestOptions} from 'angular2/http';
import {Observable}     from 'rxjs/Observable';

import {User} from './user';

@Injectable()
export class UserService
{
  constructor(private http:Http)
  {
  }

  private _loginUrl = 'http://localhost:8080/login';

  login(user:string, password:string)
  {
    return this.http.post(this._loginUrl, user + password).map(
      res=><User> res.json()).catch(this.handleError);
  }

  private handleError (error: Response) {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }}

