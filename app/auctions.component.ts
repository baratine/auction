import { Component } from 'angular2/core';
import { HTTP_PROVIDERS } from 'angular2/http';
import { RouteConfig, ROUTER_DIRECTIVES, ROUTER_PROVIDERS} from 'angular2/router';

import {AuctionService} from "./auction.service";
import {Auction} from "./auction";
import {AuctionListener} from "./auction";
import {OnInit} from "angular2/core";

@Component({
             selector: 'auctions',
             templateUrl: 'app/auctions.component.html',
             styleUrls: [''],
             providers: [],
             bindings: []
           })
export class AuctionsComponent implements AuctionListener, OnInit
{
  public auctions:Auction[] = [];

  constructor(private _auctionService:AuctionService)
  {
    console.log("creating new AuctionsComponent");
  }

  ngOnInit():any
  {
    this._auctionService.addAuctionListener(this);
    this._auctionService.registerForAuctionUpdates();
  }

  onNew(auction:Auction)
  {
    console.log(auction);
    this.auctions.push(auction);
  }

  onUpdate(auctions:Auction[])
  {
  }
}
