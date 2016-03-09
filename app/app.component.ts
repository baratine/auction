import { Component } from 'angular2/core';
import { HTTP_PROVIDERS } from 'angular2/http';

import {ROUTER_DIRECTIVES} from "angular2/router";
import {ROUTER_PROVIDERS} from "angular2/router";

import {User} from "./user";
import { LoginComponent } from './login.component';
import {NewAuctionComponent} from "./new-auction.component";
import {AuctionsComponent} from "./auctions.component";
import {UserService} from "./user.service";
import {AuctionService} from "./auction.service";

@Component({
             selector: 'my-app',
             template: `
             <h1>{{title}}</h1>
             <login-form></login-form>
             <new-auction></new-auction>
             <auctions></auctions>
             `,
             styleUrls: ['./app/app.component.css'],
             providers: [HTTP_PROVIDERS,
                         ROUTER_PROVIDERS,
                         UserService,
                         AuctionService,
                         AuctionsComponent],
             directives: [ROUTER_DIRECTIVES, LoginComponent, NewAuctionComponent, AuctionsComponent],
             bindings: [],
           })
export class AppComponent
{
  constructor()
  {
  }

  title = 'Baratine™ Auction Application';
  user:User;
}
