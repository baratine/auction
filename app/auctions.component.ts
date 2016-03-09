import { Component } from 'angular2/core';
import { HTTP_PROVIDERS } from 'angular2/http';
import { RouteConfig, ROUTER_DIRECTIVES, ROUTER_PROVIDERS} from 'angular2/router';
import {AuctionService} from "./auction.service";
import {Auction} from "./auction";

@Component({
             selector: 'auctions',
             templateUrl: 'app/auctions.component.html',
             styleUrls: [''],
             providers: [ROUTER_PROVIDERS],
             bindings: [AuctionService]
           })
export class AuctionsComponent
{
  public auctions:Auction[] = [{"id": "fake", "title": "fake title"}];

  constructor(private _auctionService:AuctionService)
  {

  }

  addAuction(auction:Auction)
  {
    console.log(auction);
    console.log(this.auctions);
    this.auctions.push(auction);
    //this.auctions.push(auction);
  }

  onClick()
  {
    this.auctions.push({"id": "fake 1", "title": "fake title 1"});
  }
}
