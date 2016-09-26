package rcms.utilities.daqexpert.servlets.mixin;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.mixin.IdGenerators;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "@id")
public interface RUMixIn {

	@JsonProperty("ref_fedBuilder")
	@JsonIdentityReference(alwaysAsId = true)
	abstract FEDBuilder getFedBuilder();

}
