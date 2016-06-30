System.register(['angular2/core', 'angular2/http', "angular2/router", "./baseurl", './login.component', "./new-auction.component", "./auctions.component", "./user.service", "./auction.service", "./bids.component", "./auction-admin.component"], function(exports_1) {
    var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
        var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
        if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
        else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
        return c > 3 && r && Object.defineProperty(target, key, r), r;
    };
    var __metadata = (this && this.__metadata) || function (k, v) {
        if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
    };
    var core_1, http_1, router_1, router_2, baseurl_1, login_component_1, new_auction_component_1, auctions_component_1, user_service_1, auction_service_1, bids_component_1, auction_admin_component_1;
    var UserAppComponent, AdminAppComponent;
    return {
        setters:[
            function (core_1_1) {
                core_1 = core_1_1;
            },
            function (http_1_1) {
                http_1 = http_1_1;
            },
            function (router_1_1) {
                router_1 = router_1_1;
                router_2 = router_1_1;
            },
            function (baseurl_1_1) {
                baseurl_1 = baseurl_1_1;
            },
            function (login_component_1_1) {
                login_component_1 = login_component_1_1;
            },
            function (new_auction_component_1_1) {
                new_auction_component_1 = new_auction_component_1_1;
            },
            function (auctions_component_1_1) {
                auctions_component_1 = auctions_component_1_1;
            },
            function (user_service_1_1) {
                user_service_1 = user_service_1_1;
            },
            function (auction_service_1_1) {
                auction_service_1 = auction_service_1_1;
            },
            function (bids_component_1_1) {
                bids_component_1 = bids_component_1_1;
            },
            function (auction_admin_component_1_1) {
                auction_admin_component_1 = auction_admin_component_1_1;
            }],
        execute: function() {
            UserAppComponent = (function () {
                function UserAppComponent() {
                    this.title = 'Baratine™ Auction Application';
                }
                UserAppComponent = __decorate([
                    core_1.Component({
                        selector: 'user-app',
                        template: "\n             <h1>{{title}}</h1>\n             <div class=\"panel\">\n               <login-form></login-form>\n             </div>\n             <div class=\"panel\">\n               <new-auction></new-auction>\n               <auctions></auctions>\n             </div>\n             <div class=\"panel\">\n               <bids></bids>\n             </div>\n             ",
                        styleUrls: ['./app/app.component.css'],
                        providers: [http_1.HTTP_PROVIDERS,
                            router_2.ROUTER_PROVIDERS,
                            core_1.provide(baseurl_1.BaseUrlProvider, { useClass: baseurl_1.UserUrlProvider }),
                            auction_service_1.AuctionService,
                            user_service_1.UserService,
                            auctions_component_1.AuctionsComponent],
                        directives: [router_1.ROUTER_DIRECTIVES, login_component_1.LoginComponent, new_auction_component_1.NewAuctionComponent, auctions_component_1.AuctionsComponent, bids_component_1.BidsComponent],
                        bindings: [],
                    }), 
                    __metadata('design:paramtypes', [])
                ], UserAppComponent);
                return UserAppComponent;
            })();
            exports_1("UserAppComponent", UserAppComponent);
            AdminAppComponent = (function () {
                function AdminAppComponent() {
                    this.title = 'Baratine™ Auction Admin Application';
                }
                AdminAppComponent = __decorate([
                    core_1.Component({
                        selector: 'admin-app',
                        template: "\n             <h1>{{title}}</h1>\n             <div class=\"panel\">\n               <login-form></login-form>\n             </div>\n             <div class=\"panel\">\n                <auction-admin></auction-admin>\n             </div>\n             ",
                        styleUrls: ['./app/app.component.css'],
                        providers: [http_1.HTTP_PROVIDERS,
                            router_2.ROUTER_PROVIDERS,
                            core_1.provide(baseurl_1.BaseUrlProvider, { useClass: baseurl_1.AdminUrlProvider }),
                            user_service_1.UserService,
                            auction_service_1.AuctionService,
                            auctions_component_1.AuctionsComponent],
                        directives: [router_1.ROUTER_DIRECTIVES, login_component_1.LoginComponent, auction_admin_component_1.AuctionAdminComponent],
                        bindings: [],
                    }), 
                    __metadata('design:paramtypes', [])
                ], AdminAppComponent);
                return AdminAppComponent;
            })();
            exports_1("AdminAppComponent", AdminAppComponent);
        }
    }
});
//# sourceMappingURL=app.component.js.map