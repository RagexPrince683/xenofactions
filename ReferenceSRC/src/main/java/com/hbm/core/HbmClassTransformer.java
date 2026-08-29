package com.hbm.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.hbm.core.compat.HardcoreDarknessCompatHooks;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.launchwrapper.IClassTransformer;

/** Patches only Hardcore Darkness' constant handlers; Minecraft classes are never transformed here. */
public final class HbmClassTransformer implements IClassTransformer {
	private static final String TARGET = "lumien.hardcoredarkness.handler.AsmHandler";
	private static final String TARGET_INTERNAL = "lumien/hardcoredarkness/handler/AsmHandler";
	private static final String HOOKS = "com/hbm/core/compat/HardcoreDarknessCompatHooks";

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(basicClass == null || (!TARGET.equals(name) && !TARGET.equals(transformedName))) return basicClass;
		HardcoreDarknessCompatHooks.markDetected();
		try {
			ClassNode node = new ClassNode();
			new ClassReader(basicClass).accept(node, ClassReader.EXPAND_FRAMES);
			boolean up = patch(node, "up", "shouldOverrideUp", "overrideUp");
			boolean down = patch(node, "down", "shouldOverrideDown", "overrideDown");
			if(!up) FMLLog.warning("[HBM] Hardcore Darkness compatibility could not find up(F)F; leaving it unchanged");
			if(!down) FMLLog.warning("[HBM] Hardcore Darkness compatibility could not find down(F)F; leaving it unchanged");
			if(!up || !down) return basicClass;
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			node.accept(writer);
			HardcoreDarknessCompatHooks.markPatched();
			FMLLog.info("[HBM] Patched Hardcore Darkness up(F)F and down(F)F sky-light carrier hooks");
			return writer.toByteArray();
		} catch(Throwable failure) {
			FMLLog.warning("[HBM] Hardcore Darkness compatibility transformer failed; leaving AsmHandler unchanged: %s", failure.toString());
			return basicClass;
		}
	}

	private static boolean patch(ClassNode owner, String methodName, String predicate, String override) {
		for(MethodNode method : owner.methods) {
			if(!methodName.equals(method.name) || !"(F)F".equals(method.desc)
					|| (method.access & Opcodes.ACC_STATIC) == 0) continue;
			LabelNode original = new LabelNode();
			InsnList prefix = new InsnList();
			prefix.add(new VarInsnNode(Opcodes.FLOAD, 0));
			prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, predicate, "(F)Z", false));
			prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
			prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TARGET_INTERNAL, "enabled", "()Z", false));
			prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
			prefix.add(new VarInsnNode(Opcodes.FLOAD, 0));
			prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, override, "(F)F", false));
			prefix.add(new InsnNode(Opcodes.FRETURN));
			prefix.add(original);
			method.instructions.insert(prefix);
			return true;
		}
		return false;
	}
}
