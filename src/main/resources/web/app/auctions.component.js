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
    var AuctionsComponent;
    return {
        setters:[
            function (core_1_1) {
                core_1 = core_1_1;
            },
            function (auction_service_1_1) {
                auction_service_1 = auction_service_1_1;
            }],
        execute: function() {
            AuctionsComponent = (function () {
                function AuctionsComponent(_auctionService) {
                    this._auctionService = _auctionService;
                    this.auctions = [];
                    console.log("creating new AuctionsComponent");
                }
                AuctionsComponent.prototype.ngOnInit = function () {
                    this._auctionService.addAuctionListener(this);
                    this._auctionService.registerForAuctionUpdates();
                };
                AuctionsComponent.prototype.onNew = function (auction) {
                    console.log(auction);
                    this.auctions.push(auction);
                };
                AuctionsComponent.prototype.onUpdate = function (auctions) {
                    for (var _i = 0, auctions_1 = auctions; _i < auctions_1.length; _i++) {
                        var auction = auctions_1[_i];
                        var i = this.auctions.findIndex(function (x) { return x.id == auction.id; });
                        if (i > -1)
                            this.auctions[i] = auction;
                    }
                };
                AuctionsComponent = __decorate([
                    core_1.Component({
                        selector: 'auctions',
                        templateUrl: 'app/auctions.component.html',
                        styleUrls: [''],
                        providers: [],
                        bindings: []
                    }), 
                    __metadata('design:paramtypes', [auction_service_1.AuctionService])
                ], AuctionsComponent);
                return AuctionsComponent;
            }());
            exports_1("AuctionsComponent", AuctionsComponent);
        }
    }
});
//# sourceMappingURL=auctions.component.js.map