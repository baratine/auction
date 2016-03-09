export interface Auction
{
  id: String;
  title: string;
  bid: number;
  state: string;
}

export interface AuctionListener
{
  onNew(auctin:Auction);
}
