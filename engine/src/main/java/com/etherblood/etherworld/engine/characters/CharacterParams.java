package com.etherblood.etherworld.engine.characters;

import com.etherblood.etherworld.engine.characters.components.AttackParams;
import com.etherblood.etherworld.engine.characters.components.HurtParams;
import com.etherblood.etherworld.engine.characters.components.PhysicParams;

public record CharacterParams(PhysicParams physics, AttackParams attack, HurtParams hurt) {

}
