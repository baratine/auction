export interface Auction
{
  id: string;
  title: string;
  bid: number;
  state: string;
}

export interface AuctionListener
{
  onNew(auction:Auction);

  onUpdate(auctions:Auction[]);
}

