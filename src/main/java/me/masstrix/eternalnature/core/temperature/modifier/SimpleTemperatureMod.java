/*
 * Copyright 2020 Matthew Denton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.masstrix.eternalnature.core.temperature.modifier;

import org.bukkit.configuration.ConfigurationSection;

public class SimpleTemperatureMod implements TemperatureModifier {

    private double emission;

    public SimpleTemperatureMod(double emission) {
        this.emission = emission;
    }

    @Override
    public double getEmission() {
        return emission;
    }

    @Override
    public boolean doesMatchType(ConfigurationSection section, String key) {
        return section.isDouble(key) || section.isInt(key);
    }
}
