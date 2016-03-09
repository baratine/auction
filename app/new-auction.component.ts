import {Component} from 'angular2/core';
import {NgForm}    from 'angular2/common';

import {AppComponent} from "./app.component";
import {AuctionService} from "./auction.service";
import {AuctionsComponent} from "./auctions.component";

@Component({
             selector: 'new-auction',
             templateUrl: 'app/new-auction.component.html',
             styleUrls: [''],
             bindings: [AuctionService, AuctionsComponent]
           })
export class NewAuctionComponent
{
  constructor(private _auctionService:AuctionService,
              private _auctionsComponent:AuctionsComponent)
  {
  }

  create(title:string, bid:number)
  {
    this._auctionService.create(title, bid)
      .subscribe(result =>
                 {
                   this._auctionsComponent.addAuction(result);
                   console.log("new auction :" + result);
                 },
                 error =>
                 {
                   console.error(error);
                 });
  }
}
