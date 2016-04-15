import { Component } from 'angular2/core';
import { HTTP_PROVIDERS } from 'angular2/http';
import { RouteConfig, ROUTER_DIRECTIVES, ROUTER_PROVIDERS} from 'angular2/router';

import {AuctionService} from "./auction.service";
import {Auction} from "./auction";
import {OnInit} from "angular2/core";
import {subscribeToResult} from "rxjs/util/subscribeToResult";
import {AuctionListener} from "./auction";

@Component({
             selector: 'bids',
             templateUrl: 'app/bids.component.html',
             styleUrls: [''],
             providers: [],
             bindings: []
           })
export class BidsComponent implements OnInit, AuctionListener
{
  public bids:Auction[] = [];//[{"id":"laksdfj", "title":"j test ", "bid":3, "state":"open"}];

  constructor(private _auctionService:AuctionService)
  {
    console.log("creating new BidsComponent");
  }

  ngOnInit():any
  {
    this._auctionService.addAuctionListener(this);
  }

  push(bids:Auction[])
  {
    for (var bid of bids) {
      var i = this.bids.findIndex(x =>x.id == bid.id);
      if (i > -1)
        this.bids[i] = bid;
    }

    for (var bid of bids) {
      var i = this.bids.findIndex(x => x.id == bid.id);
      if (i == -1)
        this.bids.push(bid);
    }
  }

  public placeBid(bid:Auction)
  {
    this._auctionService.subscribe(bid).subscribe(subscribed=>
                                                  {
                                                    console.log(
                                                      "auction subscribed: "
                                                      + subscribed);
                                                  }, error=>
                                                  {
                                                    console.error(error);
                                                  });

    this._auctionService.bid(bid).subscribe(isAccepted=>
                                            {
                                              console.log("bid accepted: "
                                                          + isAccepted);
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
      var i = this.bids.findIndex(x =>x.id == bid.id);
      if (i > -1)
        this.bids[i] = bid;
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
