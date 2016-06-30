System.register(['angular2/core', "./auction.service"], function(exports_1) {
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
    var AuctionAdminComponent;
    return {
        setters:[
            function (core_1_1) {
                core_1 = core_1_1;
            },
            function (auction_service_1_1) {
                auction_service_1 = auction_service_1_1;
            }],
        execute: function() {
            AuctionAdminComponent = (function () {
                function AuctionAdminComponent(_auctionService) {
                    this._auctionService = _auctionService;
                    this.auctions = []; //[{"id":"laksdfj", "title":"j test ", "bid":3, "state":"open"}];
                    console.log("creating new AuctionAdminComponent");
                }
                AuctionAdminComponent.prototype.ngOnInit = function () {
                    this._auctionService.addAuctionListener(this);
                };
                AuctionAdminComponent.prototype.push = function (auctions) {
                    for (var _i = 0; _i < auctions.length; _i++) {
                        var auction = auctions[_i];
                        var i = this.auctions.findIndex(function (x) { return x.id == auction.id; });
                        if (i > -1)
                            this.auctions[i] = auction;
                    }
                    for (var _a = 0; _a < auctions.length; _a++) {
                        var auction = auctions[_a];
                        var i = this.auctions.findIndex(function (x) { return x.id == auction.id; });
                        if (i == -1) {
                            this.auctions.push(auction);
                            this._auctionService.subscribe(auction).subscribe(function (subscribed) {
                                console.log("auction subscribed: "
                                    + subscribed);
                            }, function (error) {
                                console.error(error);
                            });
                        }
                    }
                };
                AuctionAdminComponent.prototype.refund = function (auction) {
                    this._auctionService.refund(auction).subscribe(function (isSuccess) {
                        console.log("refunded: "
                            + isSuccess);
                    }, function (error) {
                        console.error(error);
                    });
                    return false;
                };
                AuctionAdminComponent.prototype.onNew = function (auction) {
                };
                AuctionAdminComponent.prototype.onUpdate = function (auctions) {
                    for (var _i = 0; _i < auctions.length; _i++) {
                        var auction = auctions[_i];
                        var i = this.auctions.findIndex(function (x) { return x.id == auction.id; });
                        if (i > -1)
                            this.auctions[i] = auction;
                    }
                };
                AuctionAdminComponent.prototype.search = function (query) {
                    var _this = this;
                    this._auctionService.searchAuctions(query)
                        .subscribe(function (result) {
                        _this.push(result);
                    }, function (error) {
                        console.error(error);
                    });
                };
                AuctionAdminComponent = __decorate([
                    core_1.Component({
                        selector: 'auction-admin',
                        templateUrl: 'app/auction-admin.component.html',
                        styleUrls: [''],
                        providers: [],
                        bindings: []
                    }), 
                    __metadata('design:paramtypes', [auction_service_1.AuctionService])
                ], AuctionAdminComponent);
                return AuctionAdminComponent;
            })();
            exports_1("AuctionAdminComponent", AuctionAdminComponent);
        }
    }
});
//# sourceMappingURL=auction-admin.component.js.map