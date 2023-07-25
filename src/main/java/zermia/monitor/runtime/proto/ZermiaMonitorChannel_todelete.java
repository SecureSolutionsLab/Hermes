package zermia.monitor.runtime.proto;

import zermia.coordinator.config.CoordinatorConfiguration;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ZermiaMonitorChannel_todelete {
	CoordinatorConfiguration props = new CoordinatorConfiguration();
	ManagedChannel mgChannel;

	public void ChannelCreation() {
		props.loadProperties(CoordinatorConfiguration.defaultPropertiesFilePath);
		 mgChannel = ManagedChannelBuilder.forAddress(props.getCoordinatorIP(), props.getCoordinatorPort())
				 .usePlaintext()
				 .build();
	}

	public void ChannelClose(ManagedChannel channelShutDown) {
		mgChannel.shutdown();
	}

	public ManagedChannel getChannel() {
		return mgChannel;
	}
}
