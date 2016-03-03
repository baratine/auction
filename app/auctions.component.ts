import { Component } from 'angular2/core';
import { HTTP_PROVIDERS } from 'angular2/http';
import { RouteConfig, ROUTER_DIRECTIVES, ROUTER_PROVIDERS} from 'angular2/router';

//import { HeroService } from './hero.service';

@Component({
             selector: 'my-app',
             templateUrl: 'app/login.html',
             styleUrls: ['app/app.component.css'],
             directives: [ROUTER_DIRECTIVES],
             providers: [
               HTTP_PROVIDERS,
               ROUTER_PROVIDERS]
           })
export class Auction
{
}
