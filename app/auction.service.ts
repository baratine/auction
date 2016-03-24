import {Injectable}     from 'angular2/core';
import {Http, Response, Headers, RequestOptions, URLSearchParams} from 'angular2/http';
import {Observable}     from 'rxjs/Observable';

import {Auction, AuctionListener} from './auction';
import {Request} from "angular2/http";
import {Json} from "angular2/src/facade/lang";
import {OnInit} from "angular2/core";

@Injectable()
export class AuctionService
{
  private auctionListeners:AuctionListener[] = [];

  private _createUrl = 'http://localhost:8080/createAuction';
  private _searchUrl = 'http://localhost:8080/searchAuctions';
  private _subscribeUrl = 'http://localhost:8080/addAuctionListener';
  private _bidUrl = 'http://localhost:8080/bidAuction';

  constructor(private http:Http)
  {
    console.log("creating new AuctionService: " + http);
  }

  public create(title:string, bid:number)
  {
    let urlSearchParams = new URLSearchParams();
    urlSearchParams.append('t', title);
    urlSearchParams.append('b', bid.toString());

    let body = urlSearchParams.toString();

    let headers = new Headers({'Content-Type': 'application/x-www-form-urlencoded'});
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._createUrl, body, options)
      .map(res=>res.json()).catch(this.handleError);
  }

  public searchAuctions(query:string)
  {
    let urlSearchParams = new URLSearchParams();
    urlSearchParams.append("q", query);
    let url = this._searchUrl + '?' + urlSearchParams.toString();

    return this.http.get(url).map(res=>this.map(res)).catch(this.handleError);
  }

  public addAuctionListener(listener:AuctionListener)
  {
    this.auctionListeners.push(listener);
  }

  public onAuctionCreate(auction:Auction)
  {
    for (var listener of this.auctionListeners)
      listener.onNew(auction);
  }

  public subscribe(auction:Auction)
  {
    let body = auction.id;

    let headers = new Headers({'Content-Type': 'application/x-www-form-urlencoded'});
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._subscribeUrl, body, options)
      .map(res=>res.text()).catch(this.handleError);
  }

  public bid(auction:Auction)
  {
    let price = auction.bid + 1;
    let body = Json.stringify({"auction": auction.id, "bid": price});

    let headers = new Headers({'Content-Type': 'application/json'});
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._bidUrl, body, options)
      .map(res=>res.text()).catch(this.handleError);
  }

  public update(auctions:Auction[])
  {
    for (var listener of this.auctionListeners)
      listener.onUpdate(auctions);
  }

  public registerForAuctionUpdates()
  {
    var websocket = new WebSocket("ws://localhost:8080/auction-updates");

    var self = this;
    websocket.addEventListener("message", function (e:MessageEvent)
    {
      self.auctionUpdate(self, e);
    });
  }

  private auctionUpdate(self:AuctionService, e:MessageEvent)
  {
    var auction = Json.parse(e.data);
    var auctions:Auction[] = [<Auction>auction];

    self.update(auctions);
  }

  private map(res:Response)
  {
    return res.json();
  }

  private handleError(error:Response)
  {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }
}
