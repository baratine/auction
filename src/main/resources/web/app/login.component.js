System.register(['angular2/core', './user.service', "./auction.service"], function(exports_1) {
    var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
        var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
        if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
        else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
        return c > 3 && r && Object.defineProperty(target, key, r), r;
    };
    var __metadata = (this && this.__metadata) || function (k, v) {
        if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
    };
    var core_1, user_service_1, auction_service_1;
    var LoginComponent;
    return {
        setters:[
            function (core_1_1) {
                core_1 = core_1_1;
            },
            function (user_service_1_1) {
                user_service_1 = user_service_1_1;
            },
            function (auction_service_1_1) {
                auction_service_1 = auction_service_1_1;
            }],
        execute: function() {
            LoginComponent = (function () {
                function LoginComponent(_userService, _auctionService) {
                    this._userService = _userService;
                    this._auctionService = _auctionService;
                }
                LoginComponent.prototype.login = function (user, password) {
                    var _this = this;
                    this._userService.login(user, password).subscribe(function (loggedIn) {
                        console.log("login: " + loggedIn);
                        if (loggedIn === "true") {
                            _this.message = "login successful: " + user;
                            _this._auctionService.registerForAuctionUpdates();
                        }
                        else {
                            _this.message = "login failed.";
                        }
                    }, function (error) {
                        console.error(error);
                        _this.message = "login failed: " + error;
                    });
                };
                LoginComponent.prototype.create = function (user, password) {
                    this._userService.create(user, password).subscribe(function (user) {
                        console.log(user);
                    }, function (error) {
                        console.error(error);
                    });
                };
                LoginComponent = __decorate([
                    core_1.Component({
                        selector: 'login-form',
                        templateUrl: 'app/login.component.html',
                        styleUrls: [''],
                        bindings: [user_service_1.UserService]
                    }), 
                    __metadata('design:paramtypes', [user_service_1.UserService, auction_service_1.AuctionService])
                ], LoginComponent);
                return LoginComponent;
            })();
            exports_1("LoginComponent", LoginComponent);
        }
    }
});
//# sourceMappingURL=login.component.js.map