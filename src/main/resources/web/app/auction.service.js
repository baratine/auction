System.register(['angular2/core', 'angular2/http', 'rxjs/Observable', './baseurl', "angular2/src/facade/lang"], function(exports_1, context_1) {
    "use strict";
    var __moduleName = context_1 && context_1.id;
    var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
        var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
        if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
        else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
        return c > 3 && r && Object.defineProperty(target, key, r), r;
    };
    var __metadata = (this && this.__metadata) || function (k, v) {
        if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
    };
    var core_1, http_1, Observable_1, baseurl_1, lang_1;
    var AuctionService;
    return {
        setters:[
            function (core_1_1) {
                core_1 = core_1_1;
            },
            function (http_1_1) {
                http_1 = http_1_1;
            },
            function (Observable_1_1) {
                Observable_1 = Observable_1_1;
            },
            function (baseurl_1_1) {
                baseurl_1 = baseurl_1_1;
            },
            function (lang_1_1) {
                lang_1 = lang_1_1;
            }],
        execute: function() {
            AuctionService = (function () {
                function AuctionService(http, _baseUrlProvider) {
                    this.http = http;
                    this._baseUrlProvider = _baseUrlProvider;
                    this.auctionListeners = [];
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
                AuctionService.prototype.create = function (title, bid) {
                    var urlSearchParams = new http_1.URLSearchParams();
                    urlSearchParams.append('t', title);
                    urlSearchParams.append('b', bid.toString());
                    var body = urlSearchParams.toString();
                    var headers = new http_1.Headers({
                        'Content-Type': 'application/x-www-form-urlencoded'
                    });
                    var options = new http_1.RequestOptions({ headers: headers });
                    return this.http.post(this._createUrl, body, options)
                        .map(function (res) { return res.json(); }).catch(this.handleError);
                };
                AuctionService.prototype.searchAuctions = function (query) {
                    var _this = this;
                    var urlSearchParams = new http_1.URLSearchParams();
                    urlSearchParams.append("q", query);
                    var url = this._searchUrl + '?' + urlSearchParams.toString();
                    var headers = new http_1.Headers();
                    var options = new http_1.RequestOptions({ headers: headers });
                    return this.http.get(url, options)
                        .map(function (res) { return _this.map(res); }).catch(this.handleError);
                };
                AuctionService.prototype.addAuctionListener = function (listener) {
                    this.auctionListeners.push(listener);
                };
                AuctionService.prototype.onAuctionCreate = function (auction) {
                    for (var _i = 0, _a = this.auctionListeners; _i < _a.length; _i++) {
                        var listener = _a[_i];
                        listener.onNew(auction);
                    }
                };
                AuctionService.prototype.subscribe = function (auction) {
                    var body = auction.id;
                    var headers = new http_1.Headers({
                        'Content-Type': 'application/x-www-form-urlencoded'
                    });
                    var options = new http_1.RequestOptions({ headers: headers });
                    return this.http.post(this._subscribeUrl, body, options)
                        .map(function (res) { return res.text(); }).catch(this.handleError);
                };
                AuctionService.prototype.bid = function (auction) {
                    var price = auction.bid + 1;
                    var body = lang_1.Json.stringify({ "auction": auction.id, "bid": price });
                    var headers = new http_1.Headers({
                        'Content-Type': 'application/json'
                    });
                    var options = new http_1.RequestOptions({ headers: headers });
                    return this.http.post(this._bidUrl, body, options)
                        .map(function (res) { return res.text(); }).catch(this.handleError);
                };
                AuctionService.prototype.refund = function (auction) {
                    var headers = new http_1.Headers();
                    var options = new http_1.RequestOptions({ headers: headers });
                    return this.http.post(this._refundUrl, auction.id, options)
                        .map(function (res) { return res.text(); }).catch(this.handleError);
                };
                AuctionService.prototype.update = function (auctions) {
                    for (var _i = 0, _a = this.auctionListeners; _i < _a.length; _i++) {
                        var listener = _a[_i];
                        listener.onUpdate(auctions);
                    }
                };
                AuctionService.prototype.registerForAuctionUpdates = function () {
                    var websocket = new WebSocket(this._auctionUpdatesUrl);
                    var self = this;
                    websocket.addEventListener("message", function (e) {
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
                };
                AuctionService.prototype.auctionUpdate = function (self, e) {
                    var auction = lang_1.Json.parse(e.data);
                    var auctions = [auction];
                    self.update(auctions);
                };
                AuctionService.prototype.map = function (res) {
                    return res.json();
                };
                AuctionService.prototype.handleError = function (error) {
                    console.error(error);
                    return Observable_1.Observable.throw(error.json().error || 'Server error');
                };
                AuctionService = __decorate([
                    core_1.Injectable(), 
                    __metadata('design:paramtypes', [http_1.Http, baseurl_1.BaseUrlProvider])
                ], AuctionService);
                return AuctionService;
            }());
            exports_1("AuctionService", AuctionService);
        }
    }
});
//# sourceMappingURL=auction.service.js.map