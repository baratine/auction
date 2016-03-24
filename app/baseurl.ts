import {Injectable, OnInit}     from 'angular2/core';

@Injectable()
export class BaseUrlProvider
{
  public baseUrl:string;

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
    this.baseUrl = window.location + "user/";
  }
}

