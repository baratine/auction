import { Component } from 'angular2/core';
import { HTTP_PROVIDERS } from 'angular2/http';
import { RouteConfig, ROUTER_DIRECTIVES, ROUTER_PROVIDERS} from 'angular2/router';

import {AuctionService} from "./auction.service";
import {Auction} from "./auction";
import {OnInit} from "angular2/core";
import {subscribeToResult} from "rxjs/util/subscribeToResult";
import {AuctionListener} from "./auction";

@Component({
             selector: 'auction-admin',
             templateUrl: 'app/auction-admin.component.html',
             styleUrls: [''],
             providers: [],
             bindings: []
           })
export class AuctionAdminComponent implements OnInit, AuctionListener
{
  public auctions:Auction[] = [];//[{"id":"laksdfj", "title":"j test ", "bid":3, "state":"open"}];

  constructor(private _auctionService:AuctionService)
  {
    console.log("creating new AuctionAdminComponent");
  }

  ngOnInit():any
  {
    this._auctionService.addAuctionListener(this);
    this._auctionService.registerForAuctionUpdates();
  }

  push(auctions:Auction[])
  {
    for (var auction of auctions) {
      var i = this.auctions.findIndex(x =>x.id == auction.id);
      if (i > -1)
        this.auctions[i] = auction;
    }

    for (var auction of auctions) {
      var i = this.auctions.findIndex(x => x.id == auction.id);
      if (i == -1) {
        this.auctions.push(auction);

        this._auctionService.subscribe(auction).subscribe(subscribed=>
                                                          {
                                                            console.log(
                                                              "auction subscribed: "
                                                              + subscribed);
                                                          }, error=>
                                                          {
                                                            console.error(error);
                                                          });
      }
    }
  }

  public refund(auction:Auction)
  {
    this._auctionService.refund(auction).subscribe(isSuccess=>
                                                   {
                                                     console.log(
                                                       "refunded: "
                                                       + isSuccess);
                                                   }, error=>
                                                   {
                                                     console.error(error);
                                                   });

    return false;
  }

  onNew(auction:Auction)
  {
  }

  onUpdate(auctions:Auction[])
  {
    for (var bid of auctions) {
      var i = this.auctions.findIndex(x =>x.id == auctions.id);
      if (i > -1)
        this.auctions[i] = bid;
    }
  }

  public search(query:string)
  {
    this._auctionService.searchAuctions(query)
      .subscribe(result=>
                 {
                   this.push(result);
                 }, error =>
                 {
                   console.error(error);
                 });
  }
}
