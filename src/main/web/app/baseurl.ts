import {Injectable, OnInit}     from 'angular2/core';

@Injectable()
export class BaseUrlProvider
{
  public url:string;
  public wsUrl:string;

  ngOnInit():any
  {
    console.log("BaseUrlProvider Init " + window);
  }
}

@Injectable()
export class UserUrlProvider extends BaseUrlProvider
{
  constructor()
  {
    super();

    this.url = "http://" + window.location.host + "/user/";
    this.wsUrl = "ws://" + window.location.host + "/user/";
  }
}

@Injectable()
export class AdminUrlProvider extends BaseUrlProvider
{
  constructor()
  {
    super();

    this.url = "http://" + window.location.host + "/admin/";
    this.wsUrl = "ws://" + window.location.host + "/admin/";
  }
}
