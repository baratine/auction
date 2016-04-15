import {Injectable}     from 'angular2/core';
import {Http, Response, Headers, RequestOptions, URLSearchParams} from 'angular2/http';
import {Observable}     from 'rxjs/Observable';

import {BaseUrlProvider} from './baseurl';
import {Auction, AuctionListener} from './auction';
import {Request} from "angular2/http";
import {Json} from "angular2/src/facade/lang";
import {OnInit} from "angular2/core";

@Injectable()
export class AuctionService
{
  private auctionListeners:AuctionListener[] = [];

  private _createUrl;
  private _searchUrl;
  private _subscribeUrl;
  private _bidUrl;
  private _refundUrl;
  private _auctionUpdatesUrl;

  constructor(private http:Http, private _baseUrlProvider:BaseUrlProvider)
  {
    console.log("creating new AuctionService: " + http);
    this._createUrl = _baseUrlProvider.url + "createAuction";
    this._searchUrl = _baseUrlProvider.url + "searchAuctions";
    this._subscribeUrl = _baseUrlProvider.url + "addAuctionListener";
    this._bidUrl = _baseUrlProvider.url + "bidAuction";
    this._refundUrl = _baseUrlProvider.url + "refund";
    //this._auctionUpdatesUrl = _baseUrlProvider.url + "auction-updates";
    this._auctionUpdatesUrl = _baseUrlProvider.wsUrl + "auction-updates";
    //this._auctionUpdatesUrl = "localhost:8080/user/auction-updates";
  }

  public create(title:string, bid:number)
  {
    let urlSearchParams = new URLSearchParams();
    urlSearchParams.append('t', title);
    urlSearchParams.append('b', bid.toString());

    let body = urlSearchParams.toString();

    let headers = new Headers({
      'Content-Type': 'application/x-www-form-urlencoded'
    });
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._createUrl, body, options)
      .map(res=>res.json()).catch(this.handleError);
  }

  public searchAuctions(query:string)
  {
    let urlSearchParams = new URLSearchParams();
    urlSearchParams.append("q", query);
    let url = this._searchUrl + '?' + urlSearchParams.toString();

    let headers = new Headers();
    let options = new RequestOptions({headers: headers});

    return this.http.get(url, options)
      .map(res=>this.map(res)).catch(this.handleError);
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

    let headers = new Headers({
      'Content-Type': 'application/x-www-form-urlencoded'
    });
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._subscribeUrl, body, options)
      .map(res=>res.text()).catch(this.handleError);
  }

  public bid(auction:Auction)
  {
    let price = auction.bid + 1;
    let body = Json.stringify({"auction": auction.id, "bid": price});

    let headers = new Headers({
      'Content-Type': 'application/json'
    });
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._bidUrl, body, options)
      .map(res=>res.text()).catch(this.handleError);
  }

  public refund(auction:Auction)
  {
    let headers = new Headers();
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._refundUrl, auction.id, options)
      .map(res=>res.text()).catch(this.handleError);
  }

  public update(auctions:Auction[])
  {
    for (var listener of this.auctionListeners)
      listener.onUpdate(auctions);
  }

  public registerForAuctionUpdates()
  {
    var websocket = new WebSocket(this._auctionUpdatesUrl);

    var self = this;

    websocket.addEventListener("message", function (e:MessageEvent)
    {
      self.auctionUpdate(self, e);
    });

/*
    var self = this;
    var sock = new SockJS(this._auctionUpdatesUrl, ["ws", "http", "https"]);
    sock.onopen = function() {
      console.log('open');
    };
    sock.onmessage = function(e) {
      self.auctionUpdate(self, e);
    };
    sock.onclose = function() {
      console.log('close');
    };
*/
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
