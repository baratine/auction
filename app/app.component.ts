import { Component } from 'angular2/core';
import { HTTP_PROVIDERS } from 'angular2/http';

import { LoginComponent } from './login.component';
import {ROUTER_DIRECTIVES} from "angular2/router";
import {ROUTER_PROVIDERS} from "angular2/router";

@Component({
             selector: 'my-app',
             template: `
             <h1>{{title}}</h1>
             <login-form></login-form>
             `,
             styleUrls: ['./app/app.component.css'],
             directives: [ROUTER_DIRECTIVES, LoginComponent],
             providers: [
               HTTP_PROVIDERS,
               ROUTER_PROVIDERS]
           })
export class AppComponent
{
  title = 'Baratine™ Auction Application';
}
