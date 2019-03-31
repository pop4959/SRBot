package com.github.pop4959.srbot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandsData {

	private List<CommandConfiguration> commandConfigurations;
	private Map<String, CommandConfiguration> commandConfigurationMap = new HashMap<>();

	public class CommandConfiguration {

		private String name;
		private String description;
		private String permission;
		private boolean hidden;
		private boolean exclusive;
		private String arguments;
		private List<String> aliases;

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public String getPermission() {
			return permission;
		}

		public boolean isHidden() {
			return hidden;
		}

		public boolean isExclusive() {
			return exclusive;
		}

		public String getArguments() {
			return arguments;
		}

		public List<String> getAliases() {
			return aliases;
		}

	}

	public Map<String, CommandConfiguration> getCommandConfigurationMap() {
		return commandConfigurationMap;
	}

	public CommandConfiguration get(String name) {
		if (commandConfigurationMap.isEmpty()) {
			for (CommandConfiguration commandConfig : commandConfigurations) {
				commandConfigurationMap.put(commandConfig.name, commandConfig);
			}
		}
		return commandConfigurationMap.get(name);
	}

}
