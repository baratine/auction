import { Component } from 'angular2/core';
import { HTTP_PROVIDERS } from 'angular2/http';

import { LoginComponent } from './login.component';
import {ROUTER_DIRECTIVES} from "angular2/router";
import {ROUTER_PROVIDERS} from "angular2/router";
import {User} from "./user";
import {NewAuctionComponent} from "./new-auction.component";

@Component({
             selector: 'my-app',
             template: `
             <h1>{{title}}</h1>
             <login-form></login-form>
             <new-auction></new-auction>
             `,
             styleUrls: ['./app/app.component.css'],
             directives: [ROUTER_DIRECTIVES, LoginComponent, NewAuctionComponent],
             bindings: [],
             providers: [
               ROUTER_PROVIDERS]
           })
export class AppComponent
{
  constructor()
  {
  }

  title = 'Baratine™ Auction Application';
  user:User;

}
