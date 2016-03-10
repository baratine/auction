import { Component } from 'angular2/core';
import { HTTP_PROVIDERS } from 'angular2/http';
import { RouteConfig, ROUTER_DIRECTIVES, ROUTER_PROVIDERS} from 'angular2/router';

import {AuctionService} from "./auction.service";
import {Auction} from "./auction";
import {OnInit} from "angular2/core";

@Component({
             selector: 'bids',
             templateUrl: 'app/bids.component.html',
             styleUrls: [''],
             providers: [],
             bindings: []
           })
export class BidsComponent implements OnInit
{
  public bids:Auction[] = [];

  constructor(private _auctionService:AuctionService)
  {
    console.log("creating new AuctionsComponent");
  }

  ngOnInit():any
  {
  }

  search(query:string)
  {
    this._auctionService.searchAuctions(query)
      .subscribe(result=>
                 {
                   for (bid of result)
                   this.bids.push(bid);
                 }, error =>
                 {
                   console.error(error);
                 });
  }
}
