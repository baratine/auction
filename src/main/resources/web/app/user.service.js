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
    var UserService;
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
            UserService = (function () {
                function UserService(http, _baseUrlProvider) {
                    this.http = http;
                    this._baseUrlProvider = _baseUrlProvider;
                    console.log("creating new UserService: " + http);
                    this._createUrl = _baseUrlProvider.url + "createUser";
                    this._loginUrl = _baseUrlProvider.url + "login";
                }
                UserService.prototype.login = function (user, password) {
                    var body = 'u=' + user + '&' + 'p=' + password;
                    var headers = new http_1.Headers({
                        'Content-Type': 'application/x-www-form-urlencoded'
                    });
                    var options = new http_1.RequestOptions({ headers: headers });
                    return this.http.post(this._loginUrl, body, options)
                        .map(function (res) { return res.text(); }).catch(this.handleError);
                };
                UserService.prototype.create = function (user, password) {
                    var _this = this;
                    var body = lang_1.Json.stringify({ "user": user, "password": password });
                    var headers = new http_1.Headers({
                        'Content-Type': 'application/json'
                    });
                    var options = new http_1.RequestOptions({ headers: headers });
                    return this.http.post(this._createUrl, body, options)
                        .map(function (res) { return _this.map(res); }).catch(this.handleError);
                };
                UserService.prototype.map = function (response) {
                    console.log(response);
                    console.log("Set-Cookie: " + response.headers.get("Set-Cookie"));
                    return response.json();
                };
                UserService.prototype.handleError = function (error) {
                    console.error(error);
                    return Observable_1.Observable.throw(error.json().error || 'Server error');
                };
                UserService = __decorate([
                    core_1.Injectable(), 
                    __metadata('design:paramtypes', [http_1.Http, baseurl_1.BaseUrlProvider])
                ], UserService);
                return UserService;
            }());
            exports_1("UserService", UserService);
        }
    }
});
//# sourceMappingURL=user.service.js.map