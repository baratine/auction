import {Injectable}     from 'angular2/core';
import {Http, Response, Headers, RequestOptions, URLSearchParams} from 'angular2/http';
import {Observable}     from 'rxjs/Observable';

import {Auction, AuctionListener} from './auction';
import {Request} from "angular2/http";
import {Json} from "angular2/src/facade/lang";

@Injectable()
export class AuctionService
{
  auctionListener: AuctionListener;

  constructor(private http:Http)
  {
    console.log("creating new AuctionService: " + http);
  }

  private _createUrl = 'http://localhost:8080/createAuction';
  private _searchUrl = 'http://localhost:8080/searchAuctions';

  create(title:string, bid:number)
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

  searchAuctions(query:string)
  {
    let urlSearchParams = new URLSearchParams();
    urlSearchParams.append("q", query);
    let url = this._searchUrl + '?' + urlSearchParams.toString();

    return this.http.get(url).map(res=>this.map(res)).catch(this.handleError);
  }

  onAuctionCreate(auction:Auction)
  {
    this.auctionListener.onNew(auction);
  }

  map(res:Response)
  {
    console.log(res.text());
    return res.json();
  }

  private handleError(error:Response)
  {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }
}
