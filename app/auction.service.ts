import {Injectable}     from 'angular2/core';
import {Http, Response, Headers, RequestOptions, URLSearchParams} from 'angular2/http';
import {Observable}     from 'rxjs/Observable';

import {Auction} from './auction';
import {Request} from "angular2/http";
import {Json} from "angular2/src/facade/lang";

@Injectable()
export class AuctionService
{
  constructor(private http:Http)
  {
  }

  private _createUrl = 'http://localhost:8080/auction/create';

  create(title:string, bid:number)
  {
    let urlSearchParams = new URLSearchParams();
    urlSearchParams.append('t', title);
    urlSearchParams.append('b', bid.toString());

    let body = urlSearchParams.toString();

    let headers = new Headers({'Content-Type': 'application/x-www-form-urlencoded'});
    let options = new RequestOptions({headers: headers});

    return this.http.post(this._createUrl, body, options)
      .map(res=>res.text()).catch(this.handleError);
  }

  private handleError(error:Response)
  {
    console.error(error);
    return Observable.throw(error.json().error || 'Server error');
  }
}
