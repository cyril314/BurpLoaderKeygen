package com.fit.burpLoad;

import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.*;

import java.io.UnsupportedEncodingException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Agent implements ClassFileTransformer {

    public Agent() {
        System.out.println("    ____                      __                    __             __ __");
        System.out.println("   / ___)__  _____________   / /   ____  ____ _____/ /____ ____   / // /___ __  __ ____ _____ _____");
        System.out.println("  / __  / / / / ___/ __  /  / /   / __  / __  / __  / _  / ___/  / ,< /  __/ / / / __ `/ _  / __  /");
        System.out.println(" / /_/ / /_/ / /  / /_/ /  / /___/ /_/ / /_/ / /_/ /  __/ /     / /| /  __/ /_/ / /_/ /  __/ / / /");
        System.out.println("/_____/ __,_/_/  / .___/  /_____/ ____/ __,_/ __,_/ ___/_/     /_/ |_ ___/ __, / __, / ___/_/ /_/");
        System.out.println("                /_/                                                      /____//____/");
        System.out.println("  商业使用请购买正版软件！");
    }

    public static void premain(String agentOps, Instrumentation instrumentation) {
        instrument(instrumentation);
    }

    public static void agentmain(String agentOps, Instrumentation instrumentation) {
        instrument(instrumentation);
    }

    private static void instrument(Instrumentation instrumentation) {
        instrumentation.addTransformer(new Agent());
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> clazz, ProtectionDomain domain, byte[] classBytes) {
        try {
            if (className.equals("java/awt/Frame") || className.equals("java/awt/Dialog")) {
                return toTranslator(className, "setTitle", classBytes);
            } else if (className.equals("javax/swing/JLabel") || className.equals("javax/swing/AbstractButton") || className.equals("javax/swing/text/JTextComponent)")) {
                return toTranslator(className, "setText", classBytes);
            } else if (className.equals("javax/swing/JTabbedPane")) {
                return toTranslator(className, "addTab", classBytes);
            } else if (className.equals("javax/swing/text/AbstractDocument")) {
                return toTranslator(className, "insertString", classBytes);
            } else if (className.equals("javax/swing/JComponent")) {
                return toTranslator(className, "setToolTipText", classBytes);
            } else if (className.equals("javax/swing/JComboBox")) {
                return toTranslator(className, "addItem", classBytes);
            } else if (className.equals("javax/swing/JOptionPane")) {
                return toTranslator(className, "showOptionDialog", classBytes);
            } else if (className.equals("javax/swing/JDialog")) {
                ClassReader cr = new ClassReader(classBytes);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
                ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        if (name.equals("setTitle")) {
                            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                            return new MethodVisitor(Opcodes.ASM5, mv) {
                                @Override
                                public void visitCode() {
                                    super.visitCode();
                                    mv.visitLdcInsn("汉化标题");
                                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/awt/Frame", name, descriptor, false);
                                }
                            };
                        } else if (name.equals("setMessage")) {
                            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                            return new MethodVisitor(Opcodes.ASM5, mv) {
                                @Override
                                public void visitCode() {
                                    super.visitCode();
                                    mv.visitLdcInsn("汉化消息");
                                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", name, descriptor, false);
                                }
                            };
                        }
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    }
                };

                cr.accept(cv, 0);
                return cw.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] result = BigInt(className, classBytes);
        if (result != null) {
            return result;
        }
        result = bounty_patch(className, classBytes);
        if (result != null) {
            return result;
        }
        result = Patch(className, classBytes);
        if (result != null) {
            return result;
        }
        result = burp_patch2(className, classBytes);
        return result;
    }

    private byte[] toTranslator(String className, String methodName, byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (name.equals(methodName)) {
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                    return new MethodVisitor(Opcodes.ASM5, mv) {
                        @Override
                        public void visitCode() {
                            super.visitCode();
                            mv.visitLdcInsn("汉化标题");
                            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, name, descriptor, false);
                        }
                    };
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        };

        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    public byte[] bounty_patch(String className, byte[] classBytes) {
        if (!className.equals("feign/okhttp/OkHttpClient")) {
            return null;
        }
        ClassReader cr = new ClassReader(classBytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        for (MethodNode method : cn.methods) {
            if (("toFeignResponse".equals(method.name)) && ("(Lokhttp3/Response;Lfeign/Request;)Lfeign/Response;".equals(method.desc))) {
                InsnList srcList = method.instructions;
                AbstractInsnNode[] insnNodes = srcList.toArray();
                for (AbstractInsnNode insnNode : insnNodes) {
                    if (((insnNode instanceof MethodInsnNode)) && (insnNode.getOpcode() == 182)) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                        if ((methodInsnNode.owner.equals("feign/Response$Builder")) && (methodInsnNode.name.equals("build")) && (methodInsnNode.desc.equals("()Lfeign/Response;"))) {
                            InsnList insnList = new InsnList();
                            insnList.add(new VarInsnNode(25, 1));
                            insnList.add(new MethodInsnNode(182, "feign/Request", "url", "()Ljava/lang/String;", false));
                            insnList.add(new VarInsnNode(25, 1));
                            insnList.add(new MethodInsnNode(182, "feign/Request", "body", "()[B", false));
                            insnList.add(new MethodInsnNode(184, "com/fit/burpLoad/Agent", "BountyFilter", "(Ljava/lang/String;[B)[B", false));
                            insnList.add(new VarInsnNode(58, 2));
                            insnList.add(new VarInsnNode(25, 2));
                            LabelNode outLabel = new LabelNode();
                            insnList.add(new JumpInsnNode(198, outLabel));
                            insnList.add(new VarInsnNode(25, 2));
                            insnList.add(new MethodInsnNode(182, "feign/Response$Builder", "body", "([B)Lfeign/Response$Builder;", false));
                            insnList.add(new IntInsnNode(17, 200));
                            insnList.add(new MethodInsnNode(182, "feign/Response$Builder", "status", "(I)Lfeign/Response$Builder;", false));
                            insnList.add(outLabel);
                            srcList.insertBefore(methodInsnNode, insnList);
                        }
                    }
                }
            }
        }
        return getClassWriter(cn);
    }

    public byte[] burp_patch2(String className, byte[] classBytes) {
        if ((!className.startsWith("burp/")) || (classBytes.length < 110000)) {
            return null;
        }
        ClassReader cr = new ClassReader(classBytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        for (MethodNode method : cn.methods) {
            if ((method.desc.equals("([Ljava/lang/Object;Ljava/lang/Object;)V")) && (method.instructions.size() > 20000)) {
                InsnList insnList = method.instructions;
                int j = 0;
                for (int i = insnList.size() - 1; i > 0; i--) {
                    if ((insnList.get(i) instanceof TypeInsnNode)) {
                        TypeInsnNode typeInsnNode = (TypeInsnNode) insnList.get(i);
                        if ((typeInsnNode.getOpcode() == 187) && ("java/lang/Exception".equals(typeInsnNode.desc))) {
                            j++;
                            if (j == 2) {
                                for (int k = 0; k < 6; k++) {
                                    if ((insnList.get(i - k) instanceof JumpInsnNode)) {
                                        JumpInsnNode jumpInsnNode = (JumpInsnNode) insnList.get(i - k);
                                        method.instructions.insert(insnList.get(i - k), new JumpInsnNode(167, jumpInsnNode.label));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return getClassWriter(cn);
    }

    public byte[] Patch(String className, byte[] classBytes) {
        if ((className.startsWith("burp/")) && (classBytes.length > 110000)) {
            System.out.println("className:" + className);
            ClassReader cr = new ClassReader(classBytes);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            try {
                for (MethodNode method : cn.methods) {
                    if ((method.desc.equals("([Ljava/lang/Object;Ljava/lang/Object;)V")) && (method.instructions.size() > 20000)) {
                        InsnList insnList = method.instructions;
                        insnList.clear();
                        insnList.add(new VarInsnNode(25, 0));
                        insnList.add(new MethodInsnNode(184, "com/fit/burpLoad/Agent", "KeyFilter", "([Ljava/lang/Object;)V", false));
                        insnList.add(new InsnNode(177));
                        method.exceptions.clear();
                        method.tryCatchBlocks.clear();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getClassWriter(cn);
        }
        return null;
    }

    public byte[] BigInt(String className, byte[] classBytes) {
        if (!className.equals("java/math/BigInteger")) {
            return null;
        }
        ClassReader reader = new ClassReader(classBytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        try {
            for (MethodNode mn : node.methods) {
                if (("oddModPow".equals(mn.name)) && ("(Ljava/math/BigInteger;Ljava/math/BigInteger;)Ljava/math/BigInteger;".equals(mn.desc))) {
                    InsnList instructions = new InsnList();
                    instructions.add(new LdcInsnNode("784494550650629402578318812038873246103062429392765900863679692401515917483288444551360396993891243581323776031174104716520618196517054075399181152805062167863399993397788108526062574442957397380276947009110814837620187732300913174633871838721345699310979475106228455540662931554176848428161559382213664590949585029191542943085957823265503304336742766067624625736735254354398945046006311244707353417137449868938968730226771591246625865947893595622114503547108373009472960599444147060111971350419883686667465293379976619008764355965580465504280432683151160078556566540083447715822702727667177022111431814347374423107231482768994558297231501807470318213348545623616323683043933536716592761352026200587983508688675808140761791235890894421345168268944272003766511723863509248901907706583412647586304919169213119463168988654772466925970559627610723088498856098005959331457878486945150019623509488580895386399262569844097131075786500942530443065538693113151751368621487717116172049865065084334155464496258411943450302953590604940950912787049251686884076034812543594664806687830000981320979025230888404872529342183959392375396232582878580953197200612251616917416861184388805361800660771481139764435258505831114252491932487115517415875364419"));
                    instructions.add(new VarInsnNode(25, 2));
                    instructions.add(new MethodInsnNode(182, "java/math/BigInteger", "toString", "()Ljava/lang/String;", false));
                    instructions.add(new MethodInsnNode(182, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false));
                    LabelNode label = new LabelNode();
                    instructions.add(new JumpInsnNode(153, label));
                    instructions.add(new TypeInsnNode(187, "java/math/BigInteger"));
                    instructions.add(new InsnNode(89));
                    instructions.add(new LdcInsnNode(SignUtils.n));
                    instructions.add(new MethodInsnNode(183, "java/math/BigInteger", "<init>", "(Ljava/lang/String;)V", false));
                    instructions.add(new VarInsnNode(58, 2));
                    instructions.add(label);
                    mn.instructions.insert(instructions);
                    return getClassWriter(node);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getClassWriter(ClassNode node) {
        ClassWriter writer = new ClassWriter(3);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static void KeyFilter(Object[] obj) {
        byte[] data = (byte[]) obj[0];
        byte[] decode = Base64.getDecoder().decode(data);
        byte[] decrypt = SignUtils.decrypt(decode);
        String[] split = new String(decrypt).split("");
        System.out.println(split);
        obj[0] = Arrays.copyOf(split, split.length - 2);
        System.out.println(obj[0]);
    }

    public static byte[] BountyFilter(String url, byte[] data) {
        System.out.println("request url:" + url);
        try {
            Map<String, Object> customerMap = new HashMap<>();
            customerMap.put("email", "taiwan@china.cn");
            customerMap.put("first_name", "taiwan");
            customerMap.put("last_name", "china");
            customerMap.put("company_name", "alibaba");
            customerMap.put("phone", "+86");
            customerMap.put("reference", "love");
            if (url.equals("https://api.licensespring.com/api/v4/product_details?product=burpbountyprostripe")) {
                return ConvertJson.toJson(customerMap).getBytes();
            }
            Map<String, Object> map = new HashMap<>();
            map.put("license_type", "perpetual");
            map.put("is_trial", false);
            map.put("validity_period", "2099-12-31T20:28:33.213+05:30");
            map.put("prevent_vm", false);
            map.put("allow_overages", false);
            map.put("max_overages", 0);
            map.put("is_floating_cloud", false);
            map.put("floating_users", 0);
            map.put("floating_timeout", 0);
            Map<String, Object> pdMap = new HashMap<>();
            pdMap.put("product_name", "Burp Bounty Pro");
            pdMap.put("short_code", "burpbountyprostripe");
            pdMap.put("allow_trial", false);
            pdMap.put("trial_days", 0);
            pdMap.put("authorization_method", "license-key");

            if (url.equals("https://api.licensespring.com/api/v4/activate_license")) {
                String doSign = new String(data);
                String json = getText(doSign, "hardware_id") + "#" + getText(doSign, "license_key") + "#2099-12-31t14:58:33.213z";
                map.put("license_signature", SignUtils.getSign(json.toLowerCase()));
                map.put("max_activations", 99);
                map.put("times_activated", 99);
                map.put("transfer_count", 99);
                map.put("customer", customerMap);
                map.put("product_details", pdMap);
                return ConvertJson.toJson(map).getBytes();
            }

            if (url.startsWith("https://api.licensespring.com/api/v4/check_license?app_name=Burp")) {
                String doSign = HttpUtils.getparam(url, "hardware_id") + "#" + HttpUtils.getparam(url, "license_key") + "#2099-12-31t14:58:33.213z";
                map.put("license_signature", SignUtils.getSign(doSign.toLowerCase()));
                map.put("max_activations", 0);
                map.put("times_activated", 0);
                map.put("transfer_count", 0);
                pdMap.put("license_active", true);
                pdMap.put("license_enabled", true);
                pdMap.put("is_expired", false);
                map.put("product_details", pdMap);
                return ConvertJson.toJson(map).getBytes();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getText(String json, String label) {
        String before = "\"" + label + "\":\"";
        String after = "\"";
        int start = json.indexOf(before);
        if (start != -1) {
            start += before.length();
            int end = json.indexOf(after, start);
            return json.substring(start, end);
        }
        return null;
    }
}