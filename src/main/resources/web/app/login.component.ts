import {Component} from 'angular2/core';
import {NgForm}    from 'angular2/common';

import {User} from "./user";
import {UserService} from './user.service'
import {UserAppComponent} from "./app.component";
import {AuctionService} from "./auction.service";

@Component({
             selector: 'login-form',
             templateUrl: 'app/login.component.html',
             styleUrls: [''],
             bindings: [UserService]
           })
export class LoginComponent
{
  user:User;
  message:string;

  constructor(private _userService:UserService, private _auctionService:AuctionService)
  {
  }

  login(user:string, password:string)
  {
    this._userService.login(user, password).subscribe(
      loggedIn =>
      {
        console.log("login: " + loggedIn);

        if (loggedIn === "true") {
          this.message = "login successful: " + user;

          this._auctionService.registerForAuctionUpdates();
        }
        else {
          this.message = "login failed.";
        }
      },
      error =>
      {
        console.error(error);

        this.message = "login failed: " + error;
      });
  }

  create(user:string, password:string)
  {
    this._userService.create(user, password).subscribe(
      user =>
      {
        console.log(user);
      },
      error =>
      {
        console.error(error);
      });
  }
}
