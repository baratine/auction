import { Component, provide } from 'angular2/core';
import { HTTP_PROVIDERS } from 'angular2/http';

import {ROUTER_DIRECTIVES} from "angular2/router";
import {ROUTER_PROVIDERS} from "angular2/router";

import {BaseUrlProvider,UserUrlProvider,AdminUrlProvider} from "./baseurl";
import {User} from "./user";
import {LoginComponent} from './login.component';
import {NewAuctionComponent} from "./new-auction.component";
import {AuctionsComponent} from "./auctions.component";
import {UserService} from "./user.service";
import {AuctionService} from "./auction.service";
import {BidsComponent} from "./bids.component";

@Component({
             selector: 'user-app',
             template: `
             <h1>{{title}}</h1>
             <div class="panel">
               <login-form></login-form>
             </div>
             <div class="panel">
               <new-auction></new-auction>
               <auctions></auctions>
             </div>
             <div class="panel">
               <bids></bids>
             </div>
             `,
             styleUrls: ['./app/app.component.css'],
             providers: [HTTP_PROVIDERS,
                         ROUTER_PROVIDERS,
                         provide(BaseUrlProvider, {useClass: UserUrlProvider}),
                         UserService,
                         AuctionService,
                         AuctionsComponent],
             directives: [ROUTER_DIRECTIVES, LoginComponent, NewAuctionComponent, AuctionsComponent, BidsComponent],
             bindings: [],
           })
export class UserAppComponent
{
  constructor()
  {
  }

  title = 'Baratine™ Auction Application';
  user:User;
}

@Component({
             selector: 'admin-app',
             template: `
             <h1>{{title}}</h1>
             <div class="panel">
               <login-form></login-form>
             </div>
             `,
             styleUrls: ['./app/app.component.css'],
             providers: [HTTP_PROVIDERS,
                         ROUTER_PROVIDERS,
                         provide(BaseUrlProvider, {useClass: AdminUrlProvider}),
                         UserService,
                         AuctionService,
                         AuctionsComponent],
             directives: [ROUTER_DIRECTIVES, LoginComponent, NewAuctionComponent, AuctionsComponent, BidsComponent],
             bindings: [],
           })
export class AdminAppComponent
{
  constructor()
  {
  }

  title = 'Baratine™ Auction Admin Application';
  user:User;
}
