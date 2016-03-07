import {Component} from 'angular2/core';
import {NgForm}    from 'angular2/common';

import {User} from "./user";
import {UserService} from './user.service'

@Component({
             selector: 'login-form',
             templateUrl: 'app/login.component.html',
             styleUrls: [''],
             bindings: [UserService]
           })
export class LoginComponent
{
  user:User;

  constructor(private _userService:UserService)
  {

  }

  login(user:string, password:string)
  {
    this._userService.login(user, password).subscribe(
      user =>
      {
        console.log(user)
      },
      error =>
      {
        console.error(error)
      });
  }

  create(user:string, password:string)
  {
    this._userService.create(user, password).subscribe(
      user =>
      {
        console.log(user)
      },
      error =>
      {
        console.error(error)
      });
  }
}
