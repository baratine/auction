System.register(['angular2/core'], function(exports_1, context_1) {
    "use strict";
    var __moduleName = context_1 && context_1.id;
    var __extends = (this && this.__extends) || function (d, b) {
        for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
    var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
        var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
        if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
        else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
        return c > 3 && r && Object.defineProperty(target, key, r), r;
    };
    var __metadata = (this && this.__metadata) || function (k, v) {
        if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
    };
    var core_1;
    var BaseUrlProvider, UserUrlProvider, AdminUrlProvider;
    return {
        setters:[
            function (core_1_1) {
                core_1 = core_1_1;
            }],
        execute: function() {
            BaseUrlProvider = (function () {
                function BaseUrlProvider() {
                }
                BaseUrlProvider.prototype.ngOnInit = function () {
                    console.log("BaseUrlProvider Init " + window);
                };
                BaseUrlProvider = __decorate([
                    core_1.Injectable(), 
                    __metadata('design:paramtypes', [])
                ], BaseUrlProvider);
                return BaseUrlProvider;
            }());
            exports_1("BaseUrlProvider", BaseUrlProvider);
            UserUrlProvider = (function (_super) {
                __extends(UserUrlProvider, _super);
                function UserUrlProvider() {
                    _super.call(this);
                    this.url = "http://" + window.location.host + "/user/";
                    this.wsUrl = "ws://" + window.location.host + "/user/";
                }
                UserUrlProvider = __decorate([
                    core_1.Injectable(), 
                    __metadata('design:paramtypes', [])
                ], UserUrlProvider);
                return UserUrlProvider;
            }(BaseUrlProvider));
            exports_1("UserUrlProvider", UserUrlProvider);
            AdminUrlProvider = (function (_super) {
                __extends(AdminUrlProvider, _super);
                function AdminUrlProvider() {
                    _super.call(this);
                    this.url = "http://" + window.location.host + "/admin/";
                    this.wsUrl = "ws://" + window.location.host + "/admin/";
                }
                AdminUrlProvider = __decorate([
                    core_1.Injectable(), 
                    __metadata('design:paramtypes', [])
                ], AdminUrlProvider);
                return AdminUrlProvider;
            }(BaseUrlProvider));
            exports_1("AdminUrlProvider", AdminUrlProvider);
        }
    }
});
//# sourceMappingURL=baseurl.js.map