package api.hbm.fluid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.hbm.inventory.fluid.FluidType;

import net.minecraft.tileentity.TileEntity;

public class PipeNet implements IPipeNet {

	private boolean valid = true;
	private FluidType type;
	private List<IFluidConductor> links = new ArrayList();
	private HashSet<IFluidConnector> subscribers = new HashSet();
	
	public PipeNet(FluidType type) {
		this.type = type;
	}

	@Override
	public void joinNetworks(IPipeNet network) {
		
		if(network == this)
			return;

		for(IFluidConductor conductor : network.getLinks()) {
			conductor.setPipeNet(type, this);
			this.getLinks().add(conductor);
		}
		network.getLinks().clear();
		
		for(IFluidConnector connector : network.getSubscribers()) {
			this.subscribe(connector);
		}
		
		network.destroy();
	}

	@Override
	public List<IFluidConductor> getLinks() {
		return links;
	}

	@Override
	public HashSet<IFluidConnector> getSubscribers() {
		return subscribers;
	}

	@Override
	public IPipeNet joinLink(IFluidConductor conductor) {
		
		if(conductor.getPipeNet(type) != null)
			conductor.getPipeNet(type).leaveLink(conductor);
		
		conductor.setPipeNet(type, this);
		this.links.add(conductor);
		return this;
	}

	@Override
	public void leaveLink(IFluidConductor conductor) {
		conductor.setPipeNet(type, null);
		this.links.remove(conductor);
	}

	@Override
	public void subscribe(IFluidConnector connector) {
		this.subscribers.add(connector);
	}

	@Override
	public void unsubscribe(IFluidConnector connector) {
		this.subscribers.remove(connector);
	}

	@Override
	public boolean isSubscribed(IFluidConnector connector) {
		return this.subscribers.contains(connector);
	}

	@Override
	public long transferFluid(long fill, int pressure) {

		subscribers.removeIf(x -> 
			x == null || !(x instanceof TileEntity) || ((TileEntity)x).isInvalid() || !x.isLoaded()
		);
		
		if(this.subscribers.isEmpty())
			return fill;
		
		List<IFluidConnector> subList = new ArrayList(subscribers);
		return fairTransfer(subList, type, pressure, fill);
	}
	
	public static long fairTransfer(List<IFluidConnector> subList, FluidType type, int pressure, long fill) {
		
		if(fill <= 0) return 0;
		
		long totalReq = 0;
		
		for(IFluidConnector con : subList) {
			totalReq += con.getDemand(type, pressure);
		}
		
		if(totalReq == 0)
			return fill;
		
		long totalGiven = 0;
		
		for(IFluidConnector con : subList) {
			long req = con.getDemand(type, pressure);
			double fraction = (double)req / (double)totalReq;
			
			long given = (long) Math.floor(fraction * fill);
			
			if(given > 0) {
				totalGiven += (given - con.transferFluid(type, pressure, given));
			}
		}
		
		return fill - totalGiven;
	}

	@Override
	public FluidType getType() {
		return type;
	}

	@Override
	public void destroy() {
		this.valid = false;
		this.subscribers.clear();
		
		for(IFluidConductor con : this.links)
			con.setPipeNet(type, null);
		
		this.links.clear();
	}

	@Override
	public boolean isValid() {
		return this.valid;
	}
}
