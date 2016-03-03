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
    console.log("login: " + user + "/" + password);

    let x = this._userService.login(user, password);

    x.subscribe(
      user =>
      {
        console.log(user)
      },
      error =>
      {
        console.error(error)
      });

    console.log(x);
  }

  create(user:string, password:string)
  {
    console.log("create: " + user + "/" + password);
  }
}
