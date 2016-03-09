export interface Auction
{
  id: String;
  title: string;
}

export interface AuctionListener
{
  onNew(auctin:Auction);
}
