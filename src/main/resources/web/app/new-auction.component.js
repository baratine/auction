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
    var NewAuctionComponent;
    return {
        setters:[
            function (core_1_1) {
                core_1 = core_1_1;
            },
            function (auction_service_1_1) {
                auction_service_1 = auction_service_1_1;
            }],
        execute: function() {
            NewAuctionComponent = (function () {
                function NewAuctionComponent(_auctionService) {
                    this._auctionService = _auctionService;
                }
                NewAuctionComponent.prototype.create = function (title, bid) {
                    var _this = this;
                    this._auctionService.create(title, bid)
                        .subscribe(function (result) {
                        _this._auctionService.onAuctionCreate(result);
                        console.log("new auction :" + result);
                    }, function (error) {
                        console.error(error);
                    });
                };
                NewAuctionComponent = __decorate([
                    core_1.Component({
                        selector: 'new-auction',
                        templateUrl: 'app/new-auction.component.html',
                        styleUrls: [''],
                        bindings: []
                    }), 
                    __metadata('design:paramtypes', [auction_service_1.AuctionService])
                ], NewAuctionComponent);
                return NewAuctionComponent;
            }());
            exports_1("NewAuctionComponent", NewAuctionComponent);
        }
    }
});
//# sourceMappingURL=new-auction.component.js.map