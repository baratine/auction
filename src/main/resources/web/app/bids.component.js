System.register(['angular2/core', "./auction.service"], function(exports_1, context_1) {
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
    var core_1, auction_service_1;
    var BidsComponent;
    return {
        setters:[
            function (core_1_1) {
                core_1 = core_1_1;
            },
            function (auction_service_1_1) {
                auction_service_1 = auction_service_1_1;
            }],
        execute: function() {
            BidsComponent = (function () {
                function BidsComponent(_auctionService) {
                    this._auctionService = _auctionService;
                    this.bids = []; //[{"id":"laksdfj", "title":"j test ", "bid":3, "state":"open"}];
                    console.log("creating new BidsComponent");
                }
                BidsComponent.prototype.ngOnInit = function () {
                    this._auctionService.addAuctionListener(this);
                };
                BidsComponent.prototype.push = function (bids) {
                    for (var _i = 0, bids_1 = bids; _i < bids_1.length; _i++) {
                        var bid = bids_1[_i];
                        var i = this.bids.findIndex(function (x) { return x.id == bid.id; });
                        if (i > -1)
                            this.bids[i] = bid;
                    }
                    for (var _a = 0, bids_2 = bids; _a < bids_2.length; _a++) {
                        var bid = bids_2[_a];
                        var i = this.bids.findIndex(function (x) { return x.id == bid.id; });
                        if (i == -1)
                            this.bids.push(bid);
                    }
                };
                BidsComponent.prototype.placeBid = function (bid) {
                    this._auctionService.subscribe(bid).subscribe(function (subscribed) {
                        console.log("auction subscribed: "
                            + subscribed);
                    }, function (error) {
                        console.error(error);
                    });
                    this._auctionService.bid(bid).subscribe(function (isAccepted) {
                        console.log("bid accepted: "
                            + isAccepted);
                    }, function (error) {
                        console.error(error);
                    });
                    return false;
                };
                BidsComponent.prototype.onNew = function (auction) {
                };
                BidsComponent.prototype.onUpdate = function (auctions) {
                    for (var _i = 0, auctions_1 = auctions; _i < auctions_1.length; _i++) {
                        var bid = auctions_1[_i];
                        var i = this.bids.findIndex(function (x) { return x.id == bid.id; });
                        if (i > -1)
                            this.bids[i] = bid;
                    }
                };
                BidsComponent.prototype.search = function (query) {
                    var _this = this;
                    this._auctionService.searchAuctions(query)
                        .subscribe(function (result) {
                        _this.push(result);
                    }, function (error) {
                        console.error(error);
                    });
                };
                BidsComponent = __decorate([
                    core_1.Component({
                        selector: 'bids',
                        templateUrl: 'app/bids.component.html',
                        styleUrls: [''],
                        providers: [],
                        bindings: []
                    }), 
                    __metadata('design:paramtypes', [auction_service_1.AuctionService])
                ], BidsComponent);
                return BidsComponent;
            }());
            exports_1("BidsComponent", BidsComponent);
        }
    }
});
//# sourceMappingURL=bids.component.js.map